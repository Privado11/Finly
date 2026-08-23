# Cómo entregarle este proyecto a Claude Code

**App:** Finly · **Package name sugerido:** `com.walter.finly`

## 1. Archivos a darle (en este orden de lectura)

1. `informe-app-finanzas.md` — visión general del producto y todas las decisiones tomadas
2. `estado-backend-app-finanzas.md` — lo que **ya existe y funciona** en Supabase (no hay que crearlo, solo consumirlo)
3. `backend-app-finanzas.md` — referencia técnica completa del backend (esquema, RLS, Edge Function, contratos)
4. `frontend-app-finanzas.md` — todo lo que Claude Code debe construir: arquitectura Android, pantallas, autenticación + huella, servicio de notificaciones bancarias, cola offline, y el sistema de notificaciones locales de la app

## 2. Instrucción sugerida para dársela a Claude Code

Puedes copiar y pegar algo así como primer mensaje:

> Voy a construir **Finly**, una app Android nativa en Kotlin para finanzas personales (package name `com.walter.finly`). Ya tengo el backend completo funcionando en Supabase — no lo crees de nuevo, ni cambies el esquema salvo que yo te lo pida explícitamente.
>
> Te adjunto 4 documentos: el informe general del proyecto, el estado real del backend ya desplegado, la referencia técnica del backend, y la especificación completa de lo que debes construir en el frontend. Léelos todos antes de escribir código.
>
> Empecemos por: [setup del proyecto Kotlin + Compose + Hilt + cliente Supabase-Kotlin, según el roadmap del documento de frontend].

## 3. Datos de conexión que Claude Code va a necesitar

Estos NO están en los documentos por seguridad — pásaselos tú directamente en el chat o en un `.env` local (nunca los subas a git):

- **Project URL:** `https://arierbdlmyhoselqiczo.supabase.co`
- **Publishable key (anon/cliente):** la que ya tienes generada en el dashboard de Supabase (Project Settings → API Keys)
- **Project ref:** `arierbdlmyhoselqiczo` (útil si usa el CLI de Supabase o el MCP de Supabase también desde su lado)

## 4. Recordatorios importantes para el desarrollo

- Todo el esquema (tablas, columnas, funciones) está en **inglés** — el código Kotlin debería seguir esa misma convención de nombres para consistencia
- La app es **100% online**: no hay Room ni SQLite como base de datos principal, solo una cola local temporal para notificaciones sin conexión (documentado en la sección 5.1 del frontend)
- El secret `LLM_API_KEY` en Supabase todavía no está configurado — la Edge Function no va a poder llamar al LLM hasta que lo hagas tú desde el dashboard
- El proveedor **Google** de Supabase Auth todavía no está activado — necesitas crear las credenciales OAuth en Google Cloud Console y cargarlas en el dashboard de Supabase antes de que el botón "Continuar con Google" funcione (detalle completo en la sección 3.3 del documento de frontend)
- Cualquier cambio de esquema en Supabase, mejor coméntamelo a mí primero (yo tengo acceso directo al proyecto vía MCP) para mantener todo sincronizado con lo que ya está desplegado

## 5. Qué pedirle primero (sugerido)

1. Setup del proyecto y estructura de carpetas
2. Pantallas de Login + desbloqueo biométrico
3. CRUD manual de cuentas, categorías y transacciones (para tener la app usable ya)
4. Pantalla de "Apps monitoreadas" + `NotificationListenerService`
5. Integración con la Edge Function `parse-notification`
6. Cola offline + sincronización
7. Sistema de notificaciones locales
8. Pulido de UI/UX
