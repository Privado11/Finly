# Especificación técnica — Frontend
### Finly · Android nativo (Kotlin)

---

## 1. Stack

- **Kotlin** + **Jetpack Compose** (UI declarativa)
- **MVVM** como patrón de arquitectura
- **Hilt** para inyección de dependencias
- **Sin base de datos local completa**: la app es 100% online, Supabase (Postgres) es la única fuente de verdad
- **DataStore** (no SharedPreferences) para: token de sesión, preferencias, y la **cola local temporal** de notificaciones pendientes de sincronizar (solo mientras no hay internet)
- **Retrofit** o cliente oficial de Supabase-Kotlin para consumir la Edge Function y PostgREST
- **WorkManager** para reintentos y sincronización de la cola offline (con restricción `NetworkType.CONNECTED`)
- **BiometricPrompt API** (androidx.biometric) para el desbloqueo con huella
- **Credential Manager** (androidx.credentials + googleid) para el inicio de sesión con Google
- **Coil** para carga de íconos de apps e imágenes
- **Material Design 3**, modo oscuro con `dynamicColor` si se quiere seguir el wallpaper del usuario

---

## 2. Arquitectura de capas

```
UI (Compose) 
    ↓ observa
ViewModel (uno por pantalla, con StateFlow/UiState)
    ↓ llama a
Repository (interfaz)
    ↓ implementada por
    RemoteDataSource (Supabase: Auth, Edge Function, PostgREST)
    + ColaOfflineDataSource (DataStore, solo para notificaciones pendientes sin internet)
```

- La UI **nunca** conoce Supabase directamente, solo el ViewModel
- El ViewModel **nunca** hace lógica de red directamente, solo llama al Repository
- El Repository es responsable de: leer/escribir en Supabase, y decidir cuándo algo debe ir a la cola offline en vez de a la red

Ejemplo de contrato:
```kotlin
interface TransaccionRepository {
    fun observarTransacciones(): Flow<List<Transaccion>>
    suspend fun agregarTransaccionManual(t: Transaccion)
    suspend fun eliminarTransaccion(id: String)
}

class TransaccionRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : TransaccionRepository {
    override fun observarTransacciones(): Flow<List<Transaccion>> =
        supabase.from("transacciones").selectAsFlow(Transaccion::class)

    override suspend fun agregarTransaccionManual(t: Transaccion) {
        supabase.from("transacciones").insert(t)
    }

    override suspend fun eliminarTransaccion(id: String) {
        supabase.from("transacciones").delete { filter { eq("id", id) } }
    }
}
```

---

## 3. Flujo de autenticación y desbloqueo con huella

Requisito: login completo la primera vez; con sesión ya iniciada, desbloqueo posterior solo con huella.

### 3.1 Diagrama de estados

```
App se abre
    ↓
¿Hay token de sesión guardado (DataStore) y no expirado?
    ├── NO → Pantalla de Login/Registro
    │           ├── Email/password contra Supabase Auth
    │           └── Google Sign-In (botón "Continuar con Google")
    │           ↓ login o registro exitoso (por cualquiera de las dos vías)
    │        Guardar JWT + refresh token en DataStore
    │           ↓
    │        Ir a Home
    │
    └── SÍ → Pantalla de bloqueo biométrico
                ↓ BiometricPrompt (huella)
             ┌── Éxito → Ir a Home (reusa el JWT guardado, sin importar si la sesión se originó por email o por Google)
             └── Fallo/cancelado → quedarse en bloqueo, opción "usar contraseña" (o reintentar Google) como respaldo
```

### 3.2 Implementación del login y registro por email

```kotlin
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionStore: SessionDataStore
) {
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        guardarSesionActual()
    }

    suspend fun registrar(email: String, password: String, nombre: String, apellido: String): Result<Unit> = runCatching {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("first_name", nombre)
                put("last_name", apellido)
            }
        }
        // El trigger de Supabase crea automáticamente el registro en `users` con first_name/last_name
        guardarSesionActual()
    }

    private suspend fun guardarSesionActual() {
        val session = supabase.auth.currentSessionOrNull()
            ?: throw IllegalStateException("Sin sesión tras autenticación")
        sessionStore.guardarSesion(session.accessToken, session.refreshToken, session.expiresAt)
    }
}
```

### 3.3 Registro e inicio de sesión con Google

Supabase Auth soporta Google como proveedor OAuth de forma nativa. La app usa **Credential Manager** (la API moderna de Android, reemplaza al deprecado Google Sign-In SDK clásico) para obtener el ID token de Google, y se lo pasa a Supabase para crear/iniciar la sesión.

```kotlin
suspend fun iniciarSesionConGoogle(activity: Activity): Result<Unit> = runCatching {
    val credentialManager = CredentialManager.create(activity)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(GOOGLE_WEB_CLIENT_ID) // el Client ID de tipo "Web" configurado en Google Cloud Console
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val result = credentialManager.getCredential(activity, request)
    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

    supabase.auth.signInWith(IDToken) {
        idToken = googleIdTokenCredential.idToken
        provider = Google
    }

    guardarSesionActual()
}
```

**Si el usuario es nuevo** (primera vez que entra con esa cuenta de Google), Supabase crea el registro en `auth.users` automáticamente, y el mismo trigger `create_user_profile()` se dispara — tomando `first_name`/`last_name` desde los metadatos que Google provee (nombre y apellido de la cuenta de Google), no hace falta pedirlos manualmente.

**Requisitos de configuración (fuera del código, uno por uno):**
1. En **Google Cloud Console**: crear credenciales OAuth 2.0 — un Client ID de tipo **Web** (el que usa Supabase del lado del servidor) y uno de tipo **Android** (con el SHA-1 del keystore de firma de la app)
2. En el **dashboard de Supabase** → Authentication → Providers → **Google**: activar el proveedor y pegar el Client ID + Client Secret generados en Google Cloud Console
3. Agregar la dependencia `androidx.credentials` y `googleid` al `build.gradle`

### 3.4 Implementación del desbloqueo biométrico

```kotlin
fun mostrarPromptBiometrico(
    activity: FragmentActivity,
    onExito: () -> Unit,
    onFallo: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onExito()
        }
        override fun onAuthenticationFailed() = onFallo()
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onFallo()
    })

    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Desbloquear")
        .setSubtitle("Usa tu huella para continuar")
        .setNegativeButtonText("Usar contraseña")
        .build()

    prompt.authenticate(info)
}
```

### 3.5 Reglas importantes

- La huella **nunca reemplaza** el login inicial — solo desbloquea el uso de una sesión ya válida
- Si el `refresh_token` expira o Supabase lo invalida, se fuerza login completo de nuevo (por email o por Google, según cómo se haya autenticado originalmente), no se insiste con huella
- El JWT se guarda **cifrado** en DataStore (usar `EncryptedFile` o Android Keystore, no texto plano)
- Verificar `BiometricManager.canAuthenticate()` antes de ofrecer la opción — si el dispositivo no tiene huella configurada, cae directo a login por contraseña siempre

---

## 4. Pantallas principales

| Pantalla | Responsabilidad |
|---|---|
| **Login / Registro** | Email/password o Google Sign-In contra Supabase Auth |
| **Bloqueo biométrico** | Huella para reanudar sesión existente |
| **Home / Dashboard** | Balance general, gráfico de gastos del mes, últimas transacciones |
| **Transacciones** | Lista completa, filtros por cuenta/categoría/fecha |
| **Detalle/Edición de transacción** | Ver o corregir una transacción (incluye las que vinieron de notificación) |
| **Cuentas** | CRUD de cuentas (efectivo, banco, tarjeta, billetera) |
| **Categorías** | CRUD de categorías con ícono y color |
| **Apps monitoreadas** | Lista de apps instaladas con switch de activar/desactivar (whitelist) |
| **Bandeja de revisión** | Notificaciones que no se lograron procesar automáticamente, para clasificar a mano |
| **Presupuestos** | Definición y seguimiento de límites por categoría |
| **Reportes** | Gráficos de tendencias, exportar CSV/PDF |
| **Configuración** | Tema, biometría, permisos, cuenta |

---

## 5. Servicio de notificaciones

```kotlin
class BankNotificationListener : NotificationListenerService() {

    @Inject lateinit var whitelistRepository: WhitelistRepository
    @Inject lateinit var procesadorNotificaciones: ProcesadorNotificaciones
    @Inject lateinit var conectividad: ConectividadHelper
    @Inject lateinit var colaOffline: ColaOfflineDataSource

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            // Lectura en cada evento, nunca cacheada al iniciar el servicio
            val permitido = whitelistRepository.estaPermitido(sbn.packageName)
            if (!permitido) return@launch

            val texto = sbn.notification.extras.getString(Notification.EXTRA_TEXT) ?: return@launch

            if (conectividad.hayInternet()) {
                procesadorNotificaciones.procesar(sbn.packageName, texto)
            } else {
                // Sin conexión: se guarda en la cola local temporal, no se pierde
                colaOffline.encolar(sbn.packageName, texto)
                SincronizacionWorker.programar(applicationContext) // se dispara cuando vuelva la red
            }
        }
    }
}
```

`ProcesadorNotificaciones` es quien orquesta: llama a la Edge Function, valida el JSON, guarda en Supabase o manda a la bandeja de revisión.

### Permisos y ciclo de vida a manejar en UI
- Guiar al usuario a Ajustes para activar `Notification Access` (no hay diálogo estándar de Android para esto)
- Solicitar exención de optimización de batería (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) explicando el motivo, en el onboarding
- Mostrar en Configuración el estado real del permiso (activo/inactivo), con acceso directo a Ajustes si está inactivo

---

## 5.1 Cola offline y sincronización

Requisito: si no hay internet cuando llega la notificación, debe guardarse localmente y procesarse (LLM + guardado) apenas vuelva la conexión — evitando duplicar si el usuario ya la registró manualmente mientras tanto.

### Almacenamiento del pendiente
Estructura mínima guardada en DataStore (no es una base de datos, es una cola serializada):
```kotlin
data class NotificacionPendiente(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val texto: String,
    val capturadaEn: Instant = Instant.now()
)
```

### Worker de sincronización
```kotlin
class SincronizacionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val colaOffline: ColaOfflineDataSource,
    private val procesadorNotificaciones: ProcesadorNotificaciones
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pendientes = colaOffline.obtenerTodas()
        pendientes.forEach { p ->
            val exito = procesadorNotificaciones.procesar(p.packageName, p.texto)
            if (exito) colaOffline.eliminar(p.id)
            // si falla, se queda en la cola para el próximo intento con conexión
        }
        return Result.success()
    }

    companion object {
        fun programar(context: Context) {
            val request = OneTimeWorkRequestBuilder<SincronizacionWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "sincronizar_pendientes",
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
```

### Regla de deduplicación (clave para este flujo)

Antes de guardar la transacción resultante de una notificación pendiente, `ProcesadorNotificaciones` consulta en Supabase si ya existe una transacción **manual** con monto, cuenta y fecha cercana (ventana ±10 minutos) coincidente:

- **Si existe** → no se inserta una nueva fila; se descarta el pendiente o se marca la transacción manual como confirmada por notificación (según se prefiera conservar el `texto_original` como respaldo)
- **Si no existe** → se procesa normalmente: llamada a la Edge Function → LLM → validación → guardado en Supabase

Esto es imprescindible porque el usuario pudo haber registrado la transacción a mano mientras el celular seguía sin señal, y no se debe terminar con dos registros de lo mismo.

---

## 6. Manejo de estado y errores en UI

- Cada pantalla expone un `UiState` sellado (`Loading`, `Success`, `Error`) desde el ViewModel vía `StateFlow`
- Errores de red (Edge Function caída, sin internet) se comunican con mensajes claros. Como la app es 100% online, las **transacciones manuales también requieren conexión** para guardarse en Supabase — solo las notificaciones bancarias capturadas sin internet tienen manejo especial (cola offline, sección 5.1)
- Loading states explícitos mientras se espera respuesta del LLM o de Supabase, para no dar sensación de app congelada

---

## 7. Testing

- **Unit tests**: ViewModels (lógica de UI state), validadores de JSON de respuesta del LLM, mappers de datos
- **Tests de la cola offline**: encolar, sincronizar, y sobre todo la lógica de deduplicación (caso: notificación pendiente + transacción manual ya registrada)
- **Tests de UI (Compose)**: flujos críticos — login, desbloqueo biométrico, agregar transacción manual

---

## 8. Checklist de seguridad frontend

- [ ] JWT y refresh token guardados cifrados (Android Keystore / EncryptedFile), nunca en SharedPreferences plano
- [ ] `BiometricManager.canAuthenticate()` verificado antes de ofrecer biometría
- [ ] Opción de respaldo a contraseña si falla la huella repetidamente
- [ ] No se loguea el texto de notificaciones bancarias en logs de producción
- [ ] Certificate pinning opcional para las llamadas a Supabase (evaluar si se justifica el nivel de esfuerzo)

---

## 9. Roadmap del frontend

1. Setup del proyecto Kotlin + Compose + Hilt + cliente Supabase-Kotlin
2. Pantallas de Login/Registro (email + Google Sign-In) y desbloqueo biométrico, integradas con Supabase Auth
3. CRUD manual de transacciones, cuentas y categorías (para tener la app usable sin depender aún de notificaciones)
4. Pantalla "Apps monitoreadas" + `NotificationListenerService` con filtro de whitelist
5. Integración con la Edge Function del backend para el parseo con LLM
6. Bandeja de revisión manual
7. Dashboard, reportes y presupuestos
8. Pulido de UI/UX, modo oscuro, animaciones, widget de home screen

---

## 10. Sistema de notificaciones locales de la app

Requisito: la app debe notificar al usuario en estos eventos:
- Ingreso registrado (automático, desde notificación bancaria)
- Egreso registrado (automático, desde notificación bancaria)
- Movimiento que requiere verificación manual (cayó en la bandeja de revisión)
- Notificación bancaria guardada en cola por falta de conexión
- Conexión recuperada y sincronización completada exitosamente

Estas son **notificaciones locales** (`NotificationManager` de Android), generadas por la propia app en el momento en que ocurre cada evento — no requieren un servicio de push externo (FCM), ya que todo el procesamiento se dispara desde el propio dispositivo.

### 10.1 Canales de notificación

Android exige agrupar las notificaciones en canales (desde API 26+). Se recomienda separarlos así, para que el usuario pueda silenciar unos y no otros desde Ajustes:

```kotlin
object CanalesNotificacion {
    const val MOVIMIENTOS = "canal_movimientos"       // ingreso/egreso/transferencia registrados
    const val REVISION = "canal_revision"              // requiere verificación manual
    const val SINCRONIZACION = "canal_sincronizacion"   // cola offline / reconexión

    fun crearCanales(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(
            NotificationChannel(MOVIMIENTOS, "Movimientos registrados", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            NotificationChannel(REVISION, "Verificación manual", NotificationManager.IMPORTANCE_HIGH)
        )
        manager.createNotificationChannel(
            NotificationChannel(SINCRONIZACION, "Sincronización", NotificationManager.IMPORTANCE_LOW)
        )
    }
}
```

### 10.2 Servicio central de notificaciones

Un único punto de entrada, inyectado donde se necesite (en `ProcesadorNotificaciones`, en el `SincronizacionWorker`, etc.):

```kotlin
class NotificadorApp @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun notificar(canal: String, id: Int, titulo: String, texto: String) {
        val notif = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notif)
    }

    fun ingresoRegistrado(monto: String, comercio: String?) =
        notificar(CanalesNotificacion.MOVIMIENTOS, 1001, "Ingreso registrado", "+$monto${comercio?.let { " · $it" } ?: ""}")

    fun egresoRegistrado(monto: String, comercio: String?) =
        notificar(CanalesNotificacion.MOVIMIENTOS, 1002, "Egreso registrado", "-$monto${comercio?.let { " · $it" } ?: ""}")

    fun transferenciaRegistrada(monto: String) =
        notificar(CanalesNotificacion.MOVIMIENTOS, 1003, "Transferencia registrada", monto)

    fun requiereVerificacionManual() =
        notificar(CanalesNotificacion.REVISION, 2001, "Revisión pendiente", "Hay un movimiento que no pudimos clasificar automáticamente. Tócalo para revisarlo.")

    fun guardadoEnColaOffline() =
        notificar(CanalesNotificacion.SINCRONIZACION, 3001, "Sin conexión", "Guardamos tu movimiento y lo procesaremos cuando vuelva internet.")

    fun sincronizacionCompletada(cantidad: Int) =
        notificar(CanalesNotificacion.SINCRONIZACION, 3002, "Sincronización completa", "Se procesaron $cantidad movimiento(s) pendiente(s).")
}
```

### 10.3 Puntos de disparo

| Evento | Dónde se dispara |
|---|---|
| Ingreso / egreso / transferencia registrado | `ProcesadorNotificaciones`, justo después de guardar exitosamente en Supabase |
| Requiere verificación manual | `ProcesadorNotificaciones`, cuando la Edge Function responde `ok: false` (baja confianza o JSON inválido) |
| Guardado en cola offline | `BankNotificationListener`, en la rama donde no hay internet (sección 5.1) |
| Sincronización completada | `SincronizacionWorker`, al final de `doWork()`, notificando cuántos pendientes se procesaron con éxito (solo si `cantidad > 0`, para no notificar de más cuando la cola ya estaba vacía) |

### 10.4 Consideraciones

- Pedir el permiso `POST_NOTIFICATIONS` (obligatorio desde Android 13) durante el onboarding, junto con el resto de permisos
- La notificación de "verificación manual" debe llevar a la pantalla de Bandeja de revisión al tocarla (`PendingIntent` con deep link)
- No generar notificación de sincronización si la cola offline estaba vacía (evitar ruido innecesario)
- Estas notificaciones son visuales/informativas únicamente — no reemplazan ni interfieren con el `NotificationListenerService`, que sigue escuchando en paralelo
