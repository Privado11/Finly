# Informe de Proyecto: Finly — App de Finanzas Personales
### Especificación funcional y técnica

---

## 1. Resumen ejecutivo

Vamos a construir una aplicación Android nativa, **100% online desde el inicio**, para el registro automatizado y manual de ingresos y egresos personales. El diferencial de la app frente a cualquier gestor de gastos genérico es la **captura automática de transacciones a partir de las notificaciones bancarias del celular**, usando un modelo de lenguaje (LLM) para interpretar el contenido de esas notificaciones, con una arquitectura de backend completamente en Supabase (datos, autenticación y lógica) que protege las credenciales y centraliza toda la información.

La prioridad en esta primera fase es **seguridad, control granular sobre qué se procesa, y buenas prácticas de arquitectura**, sobre una base de datos única en Supabase — no hay fase local ni migración posterior: todo el almacenamiento vive en Supabase desde el primer día.

---

## 2. Alcance y decisiones de plataforma

| Decisión | Resolución |
|---|---|
| Nombre de la app | **Finly** |
| Package name sugerido | `com.walter.finly` |
| Plataforma objetivo | **Solo Android** (no se contempla iOS) |
| Tecnología de la app | **Kotlin nativo** (no Flutter, no .NET MAUI) |
| Almacenamiento | **App 100% online desde el inicio**: Supabase (Postgres) es la única base de datos, sin SQLite ni Room en ningún momento del proyecto |
| Manejo de offline | Solo una **cola local temporal** para notificaciones capturadas sin conexión (ver sección 3.5) — no es una base de datos completa, se vacía al sincronizar |
| Backend | **Supabase completo** (Postgres + Auth + Edge Functions) |
| Autenticación | **Registro/login con email y contraseña, y también con Google (Google Sign-In)**, ambos vía Supabase Auth. Una vez hay sesión iniciada, los accesos posteriores se validan con **huella digital**, sin pedir usuario/contraseña de nuevo |
| UI | Moderna, Material Design 3, modo oscuro, animaciones sutiles |

**Justificación de Kotlin nativo:** al descartar iOS, la funcionalidad más delicada del proyecto —la lectura de notificaciones vía `NotificationListenerService`— tiene mejor soporte, estabilidad y documentación en nativo que en Flutter o MAUI, donde esa capacidad depende de plugins o código de plataforma añadido como capa extra.

---

## 3. Funcionalidad núcleo: captura automática de transacciones

### 3.1 Flujo general

```
Notificación llega al celular
        ↓
NotificationListenerService la intercepta
        ↓
Filtro por whitelist (¿la app de origen está permitida?)
        ↓
   NO → se descarta inmediatamente, nunca sale de la app
        ↓
   SÍ → ¿hay conexión a internet?
        ↓
   NO → se guarda en la cola local temporal de pendientes (ver 3.5)
        ↓ (cuando vuelve la conexión)
   SÍ → se envía el texto a la Edge Function de Supabase
        ↓
   Edge Function llama al LLM (ej. Claude Haiku) → devuelve JSON estructurado
        ↓
   App valida el JSON y lo guarda directamente en Supabase (Postgres)
        ↓
   Si algo falla en cualquier paso → cae en "bandeja de revisión manual" (también en Supabase)
```

### 3.2 Whitelist de apps monitoreadas — control total del usuario

Requisito explícito: **poder agregar o eliminar apps monitoreadas sin necesidad de hacer un nuevo build.**

- Pantalla dedicada "Apps monitoreadas" que lista todas las apps instaladas en el dispositivo (`PackageManager.getInstalledApplications`)
- Cada app se muestra con ícono real, nombre y un switch de activar/desactivar
- Buscador para encontrar rápido entre las apps instaladas
- La whitelist se persiste en una tabla (`allowed_apps`), leída **en cada evento de notificación**, nunca cacheada en memoria al arrancar el servicio — así los cambios aplican de inmediato, sin reiniciar la app
- Solo las apps marcadas como bancarias/financieras deben activarse aquí; todo lo demás (WhatsApp, redes sociales, etc.) se descarta en el filtro local, **sin salir nunca a internet**

### 3.5 Manejo de notificaciones sin conexión a internet

Dado que la app es 100% online (sin base de datos local completa), es indispensable no perder información cuando el celular no tiene internet en el momento exacto en que llega una notificación bancaria.

- Toda notificación que pase el filtro de whitelist pero **no pueda enviarse de inmediato** (sin conexión) se guarda en una **cola local temporal** (no es una base de datos de la app, es solo un buffer de pendientes — puede ser tan simple como una tabla mínima local o un archivo de cola)
- Un `WorkManager` con restricción de conectividad (`NetworkType.CONNECTED`) reintenta automáticamente en cuanto vuelve la conexión: toma cada pendiente, la envía a la Edge Function, valida el JSON y la guarda en Supabase
- **Regla de no duplicidad**: antes de guardar una transacción que viene de la cola offline, la app verifica que el usuario no la haya registrado ya manualmente mientras tanto (comparando monto, cuenta y ventana de tiempo cercana). Si ya existe una coincidencia manual, la notificación de la cola se descarta o se vincula a la transacción existente, en vez de duplicarla
- Si tras procesar la cola alguna notificación falla igual (JSON inválido, etc.), cae en la bandeja de revisión manual como cualquier otro caso
- La cola se vacía notificación por notificación a medida que se confirma su guardado exitoso en Supabase — nunca se acumula como almacenamiento permanente

### 3.3 Interpretación con LLM — no regex

**Decisión:** usar un LLM para interpretar el texto de las notificaciones bancarias, en vez de mantener un motor de regex por banco.

Razones que sustentan la decisión:
- Volumen esperado bajo (100–500 llamadas al mes) → costo insignificante
- Mayor resiliencia a cambios de formato de los bancos, sin mantenimiento manual de patrones
- No hay objeción de privacidad: se acepta que el contenido de las notificaciones bancarias (montos, comercios, tipo de movimiento) salga hacia el LLM

Requisitos de implementación:
- El LLM se invoca **únicamente** para notificaciones que ya pasaron el filtro de whitelist — nunca para el resto
- La respuesta del LLM debe ser **JSON estructurado** (monto, tipo de movimiento, comercio/destino, moneda), nunca texto libre a interpretar después
- La captura automática cubre los **tres tipos de movimiento**: ingreso, egreso, y **transferencia entre cuentas propias** (ej. traspasar plata de la cuenta de ahorros a una billetera digital) — no es exclusiva de los egresos
- Toda respuesta del LLM se valida antes de guardarse (tipos correctos, monto numérico, campos obligatorios presentes) — no se confía ciegamente en la salida
- Si el JSON no pasa validación o el LLM falla, la notificación cruda cae en la bandeja de revisión manual

### 3.4 Backend: Supabase

**Decisión:** Supabase será el **backend total de la aplicación** desde el inicio (no solo para el parseo con LLM). Esto incluye autenticación y **todo el almacenamiento de datos** (transacciones, cuentas, categorías, whitelist) — no existe una fase local previa ni una migración posterior.

Componentes:
- **Supabase Auth**: gestiona el inicio de sesión (email/password). La sesión emite un JWT que la app conserva; mientras sea válida, el acceso subsecuente se valida con huella (ver sección 4)
- **Edge Function** (`POST /parse-notification`): recibe el texto de la notificación, invoca al LLM, devuelve el JSON parseado
- La **API key del LLM vive como secret de Supabase**, nunca embebida en el APK
- Solo la app autenticada (JWT válido) puede invocar la función
- El resto del CRUD de la app (transacciones, cuentas, categorías) no necesita Edge Functions adicionales: se consume directo vía PostgREST, el API REST que Supabase genera automáticamente sobre las tablas, respetando siempre RLS

---

## 4. Seguridad — nivel máximo

Requisito explícito: seguridad máxima en todas las capas.

- **Row Level Security (RLS)** activo en todas las tablas de Supabase desde el día uno (transacciones, cuentas, whitelist), aunque hoy solo haya un usuario
- **Autenticación por JWT** en la Edge Function — rechazar cualquier llamada no autenticada
- **Rate limiting** en la Edge Function, para que si el APK es decompilado y alguien extrae endpoints, no se pueda abusar del backend
- **Flujo de acceso a la app**: la primera vez (o si no hay sesión activa) se pide login completo contra Supabase Auth. Mientras exista una sesión válida (token no expirado), los siguientes accesos a la app se validan solo con **huella digital** — la huella desbloquea localmente el uso de la sesión ya iniciada, no crea una sesión nueva
- Manejo cuidadoso del permiso `BIND_NOTIFICATION_LISTENER_SERVICE` (se activa manualmente en Ajustes del sistema, no se puede pedir por diálogo estándar)

---

## 5. Arquitectura y buenas prácticas de código

- **MVVM** como patrón de presentación, con Jetpack Compose para la UI
- **Repository pattern** para toda la capa de datos: la lógica de negocio nunca habla directo con Supabase — habla con una interfaz. Esto mantiene desacoplada la UI de los detalles de red y facilita testing y manejo de la cola offline
- **Inyección de dependencias** desde el inicio (Hilt es el estándar recomendado en Android nativo)
- Modelo de datos con **IDs tipo UUID**, campos `created_at` / `updated_at` en cada tabla (generados por Postgres)
- **Strategy pattern** aplicable al procesamiento de notificaciones: una interfaz común (`TransactionParser` o equivalente), de forma que agregar soporte a un caso nuevo sea aislado y no toque el resto del sistema
- **Tests unitarios** para la lógica de parsing y de validación del JSON del LLM — es la parte más frágil y la que más va a iterar
- **WorkManager** para cualquier trabajo diferido o reintentos (ej. si falla la llamada al LLM), nunca reintentos bloqueantes dentro del propio listener
- **Logging estructurado** para poder depurar en producción qué notificaciones no se procesaron correctamente
- Manejo de errores robusto en toda la cadena: caída de Supabase, JSON malformado del LLM, sin internet — ninguno de estos casos debe crashear la app; todos deben degradar hacia la bandeja de revisión manual

---

## 6. Funcionalidades adicionales recomendadas

### Gestión financiera
- Transacciones, categorías personalizables (ícono + color), cuentas múltiples (efectivo, bancos, tarjetas, billeteras digitales)
- Presupuestos por categoría/mes con alertas
- Transacciones recurrentes (suscripciones, pagos fijos) con recordatorios
- Reportes visuales: gastos por categoría, tendencias mensuales, balance general

### Inteligencia y usabilidad
- Detección de duplicados (misma compra notificada por banco y billetera)
- Detección automática de patrones recurrentes (mismo comercio/monto todos los meses)
- Categorización automática sugerida por comercio, corregible por el usuario

### Datos y respaldo
- Exportación a Excel/CSV/PDF (útil para declaración de renta y análisis externo)
- Backup/exportación de los datos desde Supabase (respaldo periódico o exportación manual bajo demanda)

### UX adicional
- Widget de balance en la pantalla de inicio
- Onboarding que guíe la activación del permiso de notificaciones y la selección inicial de apps bancarias

---

## 7. Consumo de batería — análisis

- El `NotificationListenerService` en sí es de **bajo impacto**: es reactivo, no hace polling, el sistema ya está despierto procesando la notificación de todas formas
- El riesgo real no es el consumo de batería sino la **confiabilidad**: Android (Doze mode / App Standby) puede matar el servicio en segundo plano si no se gestiona bien
- Con el volumen estimado (10–20 notificaciones bancarias al día), el consumo de red por las llamadas al LLM es insignificante
- Acción concreta: solicitar `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` explicando al usuario por qué, durante el onboarding, y usar `WorkManager` para cualquier trabajo diferido — nunca lógica pesada síncrona dentro del listener

---

## 8. Próximos pasos sugeridos

1. Definir el modelo de datos completo en Supabase/Postgres (transacciones, cuentas, categorías, whitelist)
2. Diseñar el flujo de autenticación de la app contra Supabase Auth
3. Levantar el esqueleto del proyecto en Supabase: tablas, políticas RLS, Edge Function inicial
4. Construir la pantalla de "Apps monitoreadas" como primer entregable de UI
5. Implementar el `NotificationListenerService` con el filtro de whitelist
6. Conectar la Edge Function con el LLM y definir el prompt de extracción estructurada
7. Priorizar qué queda en el MVP (v1) vs qué se deja para una fase posterior, de la lista de funcionalidades adicionales
