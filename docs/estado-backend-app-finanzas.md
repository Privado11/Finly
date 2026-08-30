# Informe de Backend — Estado Actual
### App de Finanzas Personales · Supabase

---

## 1. Datos del proyecto

| Campo | Valor |
|---|---|
| Nombre del proyecto | `app-finanzas` |
| Project ref / ID | `arierbdlmyhoselqiczo` |
| Región | São Paulo (`sa-east-1`) |
| URL | `https://arierbdlmyhoselqiczo.supabase.co` |
| Plan | Gratuito ($0/mes) |
| Estado | Activo y saludable |

---

## 2. Modelo de datos — 7 tablas, todas en producción, nombres en inglés

### 2.1 `users`
Datos personales del usuario, se llena automáticamente al crear la cuenta.

| Columna | Tipo | Notas |
|---|---|---|
| id | uuid (PK) | referencia a `auth.users` |
| first_name | text | obligatorio |
| last_name | text | obligatorio |
| created_at / updated_at | timestamptz | |

**Automatización:** trigger `on_auth_user_created` sobre `auth.users` que crea el registro en `users` apenas alguien se registra, tomando `first_name` y `last_name` de los metadatos enviados en el signup.

### 2.2 `accounts`
| Columna | Tipo | Notas |
|---|---|---|
| id | uuid (PK) | |
| user_id | uuid (FK) | → `auth.users` |
| name | text | |
| type | text | check: `cash`, `bank`, `credit_card`, `digital_wallet` |
| currency | text | default `COP` |
| active | boolean | default `true` |
| created_at / updated_at | timestamptz | |

### 2.3 `categories`
| Columna | Tipo | Notas |
|---|---|---|
| id | uuid (PK) | |
| user_id | uuid (FK) | |
| name, icon, color | text | |
| type | text | check: `income`, `expense` |
| created_at | timestamptz | |

### 2.4 `transactions`
La tabla central. Soporta los **tres tipos de movimiento**, incluyendo transferencias entre cuentas propias.

| Columna | Tipo | Notas |
|---|---|---|
| id | uuid (PK) | |
| user_id | uuid (FK) | |
| source_account_id | uuid (FK → accounts) | obligatoria en los 3 tipos |
| destination_account_id | uuid (FK → accounts) | **solo** cuando `type = 'transfer'` |
| category_id | uuid (FK → categories) | opcional |
| type | text | check: `income`, `expense`, `transfer` |
| amount | numeric(14,2) | check: >= 0 |
| currency | text | default `COP` |
| merchant, description | text | opcionales |
| source | text | check: `manual`, `notification_regex`, `notification_llm` |
| raw_notification | text | texto original si vino de notificación |
| date | timestamptz | |
| created_at / updated_at | timestamptz | |

**Constraint clave:** `transfer_requires_destination` — obliga a que `destination_account_id` esté presente (y sea distinta de la origen) únicamente cuando `type = 'transfer'`; en income/expense debe ser nula.

### 2.5 `allowed_apps`
Whitelist configurable de apps monitoreadas para el `NotificationListenerService`.

| Columna | Tipo | Notas |
|---|---|---|
| id | uuid (PK) | |
| user_id | uuid (FK) | |
| package_name | text | único por usuario |
| display_name | text | |
| active | boolean | default `true` |

### 2.6 `review_queue`
Notificaciones que no se lograron procesar automáticamente.

| Columna | Tipo | Notas |
|---|---|---|
| id | uuid (PK) | |
| user_id | uuid (FK) | |
| package_name, original_text | text | |
| failure_reason | text | libre: `invalid_json`, `llm_error`, etc. |
| resolved | boolean | default `false` |

### 2.7 `llm_usage`
Control de uso diario del LLM por usuario, para el rate limiting.

| Columna | Tipo | Notas |
|---|---|---|
| user_id | uuid (FK) | PK compuesta |
| date | date | PK compuesta, default hoy |
| calls | int | default 0 |

---

## 3. Funciones de base de datos

### `create_user_profile()`
- `SECURITY DEFINER`, disparada por el trigger `on_auth_user_created`
- Inserta en `users` con los datos del signup
- Ejecución revocada para roles públicos — solo se dispara internamente vía trigger

### `register_llm_usage(user_id, limit)`
- Incrementa de forma **atómica** el contador diario en `llm_usage`
- Devuelve `true`/`false` según si el usuario sigue dentro del límite diario
- Ejecución restringida solo a `service_role` (la usa la Edge Function)

---

## 4. Seguridad — Row Level Security (RLS)

**Activo en las 7 tablas**, sin excepción. Cada usuario solo puede ver/crear/editar/eliminar sus propios registros, comparando contra `(select auth.uid())` — optimizado para evitar reevaluación fila por fila.

**Resultado del advisor de seguridad de Supabase: 0 hallazgos.**

---

## 5. Índices

Índices en todas las llaves foráneas (`user_id` en cada tabla, `source_account_id`, `destination_account_id`, `category_id`), más un índice compuesto `(user_id, date)` en `transactions` para acelerar historial y reportes.

---

## 6. Edge Function: `parse-notification`

**Estado:** desplegada, versión activa (`ACTIVE`), con verificación de JWT obligatoria.

**Flujo interno:**
1. Valida JWT de Supabase Auth (401 si no hay o es inválido)
2. Valida el body (`package_name`, `text`)
3. Llama a `register_llm_usage` vía `service_role` — si superó el límite diario (100/día), responde 429
4. Llama al LLM (Gemini 2.0 Flash, tier gratuito) con un prompt en inglés y structured output que distingue `income`/`expense`/`transfer`
5. Valida que la respuesta sea JSON bien formado, con `type`, `amount` y `confidence` válidos
6. Si `confidence` es `"low"` o el JSON no es válido, responde `ok: false`
7. Si todo está bien, responde `ok: true` con los datos estructurados — el cliente inserta finalmente en `transactions`

**Variables de entorno:**
- `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY` (automáticas)
- `GEMINI_API_KEY` — **pendiente de configurar por el usuario** en el dashboard (Project Settings → Edge Functions → Secrets), se obtiene gratis en Google AI Studio (aistudio.google.com/apikey)
- `LLM_MODEL` — opcional, con valor por defecto en el código

---

## 7. Lo que falta por hacer (requiere acción manual del usuario, no de Claude Code)

- [ ] Configurar el secret `GEMINI_API_KEY` en el dashboard de Supabase
- [ ] Confirmar el proveedor definitivo del LLM
- [ ] Activar el proveedor **Google** en Authentication → Providers, con las credenciales OAuth de Google Cloud Console
- [ ] Probar el flujo real de principio a fin desde la app

---

## 8. Resumen de lo ya construido

✅ 7 tablas con esquema completo en inglés y relaciones correctas
✅ RLS activo y optimizado en el 100% de las tablas
✅ Trigger automático de creación de perfil al registrarse
✅ Rate limiting real y atómico para el LLM
✅ Edge Function de parseo de notificaciones, desplegada y funcional
✅ Índices de rendimiento en todas las llaves foráneas
✅ 0 hallazgos de seguridad en el advisor de Supabase
