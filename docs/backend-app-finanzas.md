# Especificación técnica — Backend
### Finly · Supabase

---

## 1. Stack

- **Supabase** como backend as a service: Postgres + Auth + Edge Functions
- **Edge Functions** en Deno/TypeScript
- **LLM**: Gemini 2.0 Flash (Google AI Studio, tier gratuito) para extracción estructurada, con structured output nativo (`responseSchema`)
- Conexión desde la app Android vía cliente oficial de Supabase para Kotlin, o llamadas REST directas autenticadas con JWT

> Nota: todos los nombres de tablas, columnas y funciones están en **inglés**, por decisión explícita del proyecto.

---

## 2. Modelo de datos (Postgres)

### 2.1 Tabla `users`
Gestionada junto con Supabase Auth (`auth.users`). Contiene los datos personales del perfil.

```sql
create table public.users (
    id uuid primary key references auth.users(id) on delete cascade,
    first_name text not null,
    last_name text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
```
Se llena automáticamente vía trigger al crear la cuenta (ver sección 3).

### 2.2 Tabla `accounts`
```sql
create table public.accounts (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    name text not null,
    type text not null check (type in ('cash','bank','credit_card','digital_wallet')),
    currency text not null default 'COP',
    opening_balance numeric(14,2) not null default 0,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
```
`opening_balance`: saldo con el que arranca la cuenta al crearla — **no es una transacción**, es el punto de partida del cálculo de balance. Editable después desde la app.

### 2.3 Tabla `categories`
```sql
create table public.categories (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    name text not null,
    icon text,
    color text,
    type text not null check (type in ('income','expense')),
    created_at timestamptz not null default now()
);
```

### 2.4 Tabla `transactions`

Distingue tres tipos: **income**, **expense** y **transfer** entre cuentas. Para transferencias se necesitan dos cuentas (origen y destino); para income/expense solo la de origen.

```sql
create table public.transactions (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    source_account_id uuid not null references public.accounts(id) on delete cascade,
    destination_account_id uuid references public.accounts(id) on delete cascade,  -- solo para type='transfer'
    category_id uuid references public.categories(id) on delete set null,
    type text not null check (type in ('income','expense','transfer')),
    amount numeric(14,2) not null check (amount >= 0),
    currency text not null default 'COP',
    merchant text,
    description text,
    source text not null check (source in ('manual','notification_regex','notification_llm')),
    raw_notification text,          -- texto original si vino de notificación
    date timestamptz not null default now(),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),

    constraint transfer_requires_destination check (
        (type = 'transfer' and destination_account_id is not null and destination_account_id <> source_account_id)
        or (type <> 'transfer' and destination_account_id is null)
    )
);

create index idx_transactions_user_date on public.transactions (user_id, date desc);
```

**Los tres tipos pueden originarse tanto de una notificación como de registro manual** — no es exclusivo de los egresos:
- Un **income** detectado por notificación (ej. "Te llegó una transferencia de $X")
- Un **expense** detectado por notificación (ej. "Compra por $X en...")
- Un **transfer** entre tus propias cuentas detectado por notificación — en este caso el LLM debe identificar ambas cuentas involucradas cuando el texto lo permita

### 2.5 Tabla `allowed_apps` (whitelist)
```sql
create table public.allowed_apps (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    package_name text not null,
    display_name text,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    unique (user_id, package_name)
);
```

### 2.6 Tabla `review_queue`
Para notificaciones que no se lograron parsear con confianza.
```sql
create table public.review_queue (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    package_name text not null,
    original_text text not null,
    failure_reason text,             -- 'invalid_json' | 'llm_error' | etc.
    resolved boolean not null default false,
    created_at timestamptz not null default now()
);
```
> Nota: la **cola de pendientes sin conexión** vive del lado del cliente (dispositivo), no en Supabase — mientras no hay internet, no hay forma de escribir en Postgres. Ver el documento de Frontend para el detalle de esa cola local temporal. Aquí en el backend solo llega el resultado final: transacción guardada, o caída en `review_queue` si falla tras reintentar.

### 2.7 Tabla `llm_usage`
Control de uso diario del LLM por usuario, para el rate limiting.
```sql
create table public.llm_usage (
    user_id uuid not null references auth.users(id) on delete cascade,
    date date not null default current_date,
    calls int not null default 0,
    primary key (user_id, date)
);
```

> Nota de diseño: todos los IDs son UUID (no serial). Todas las tablas incluyen `user_id` desde ya, aunque hoy solo exista un usuario — así RLS queda listo sin refactor futuro.

### 2.8 Vista `account_balances`

Los saldos **no se guardan** en `accounts` — se calculan a partir de las transacciones. Esta vista hace ese cálculo por el usuario, para que el frontend nunca tenga que sumar transacciones a mano.

```sql
create view public.account_balances
with (security_invoker = true) as
select
    a.id as account_id,
    a.user_id,
    a.name,
    a.type,
    a.currency,
    a.active,
    a.opening_balance,
    a.opening_balance + coalesce(sum(
        case
            when t.source_account_id = a.id and t.type = 'income'   then  t.amount
            when t.source_account_id = a.id and t.type = 'expense'  then -t.amount
            when t.source_account_id = a.id and t.type = 'transfer' then -t.amount
            when t.destination_account_id = a.id and t.type = 'transfer' then t.amount
            else 0
        end
    ), 0) as balance
from public.accounts a
left join public.transactions t
    on t.source_account_id = a.id or t.destination_account_id = a.id
group by a.id, a.user_id, a.name, a.type, a.currency, a.active, a.opening_balance;
```

**Lógica del cálculo:**
- `income` → suma a la cuenta origen
- `expense` → resta de la cuenta origen
- `transfer` → resta de la cuenta origen, suma a la cuenta destino
- Todo parte de `opening_balance`, no de cero

**`security_invoker = true`** es la parte importante: hace que la vista respete el RLS de `accounts` y `transactions` del usuario que consulta — sin esto, una vista corre con los permisos de quien la creó, no del usuario logueado, lo cual sería un hueco de seguridad.

El "Patrimonio total" de la pantalla de Cuentas es simplemente la suma de `balance` de las filas con `active = true` que devuelve esta vista — se calcula en el cliente, no hace falta otra consulta aparte.

---

## 3. Row Level Security (RLS)

RLS **activo en las 7 tablas**, sin excepción. Patrón uniforme por tabla (select/insert/update/delete según aplique), optimizado usando `(select auth.uid())` en vez de `auth.uid()` directo para que Postgres no reevalúe la función fila por fila:

```sql
alter table public.accounts enable row level security;

create policy "select own"
on public.accounts for select
using ((select auth.uid()) = user_id);

create policy "insert own"
on public.accounts for insert
with check ((select auth.uid()) = user_id);

create policy "update own"
on public.accounts for update
using ((select auth.uid()) = user_id);

create policy "delete own"
on public.accounts for delete
using ((select auth.uid()) = user_id);
```

Mismo patrón aplicado en `transactions`, `categories`, `allowed_apps`, `review_queue`, `llm_usage` (esta última sin delete), y variante de solo select/insert/update en `users`.

---

## 4. Trigger de creación de perfil

```sql
create or replace function public.create_user_profile()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  insert into public.users (id, first_name, last_name)
  values (
    new.id,
    coalesce(new.raw_user_meta_data->>'first_name', ''),
    coalesce(new.raw_user_meta_data->>'last_name', '')
  );
  return new;
end;
$$;

revoke all on function public.create_user_profile() from public, anon, authenticated;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.create_user_profile();
```

Se dispara automáticamente al hacer signup — el cliente Android debe enviar `first_name` y `last_name` como metadata del signup (ver documento de Frontend).

---

## 5. Edge Function: `parse-notification`

### 5.1 Contrato

**Request** (autenticado con JWT de Supabase, header `Authorization: Bearer <token>`):
```json
POST /functions/v1/parse-notification
{
  "package_name": "com.bancolombia.app",
  "text": "Bancolombia le informa Compra por $45.000 en RAPPI COLOMBIA"
}
```

**Response (éxito):**
```json
{
  "ok": true,
  "data": {
    "type": "expense",
    "amount": 45000,
    "currency": "COP",
    "merchant": "Rappi",
    "suggested_destination_account": null,
    "confidence": "high"
  }
}
```

**Response (fallo/baja confianza):**
```json
{
  "ok": false,
  "reason": "invalid_json_or_low_confidence",
  "original_text": "..."
}
```
→ el cliente, al recibir `ok: false`, inserta directamente en `review_queue`.

### 5.2 Pseudocódigo de la función

```typescript
import { createClient } from '@supabase/supabase-js'

Deno.serve(async (req) => {
  // 1. Validar JWT
  const authHeader = req.headers.get('Authorization')
  const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
    global: { headers: { Authorization: authHeader } }
  })
  const { data: { user }, error } = await supabase.auth.getUser()
  if (error || !user) return new Response('Unauthorized', { status: 401 })

  // 2. Rate limiting atómico vía RPC (service role)
  const withinLimit = await registerLLMUsage(user.id)
  if (!withinLimit) return new Response('Rate limit exceeded', { status: 429 })

  // 3. Parsear body
  const { package_name, text } = await req.json()

  // 4. Llamar al LLM con prompt de extracción estructurada
  const result = await callLLM(text)

  // 5. Validar estructura de la respuesta
  if (!validateJSON(result)) {
    return Response.json({ ok: false, reason: 'invalid_json_or_low_confidence', original_text: text })
  }

  // 6. Responder
  return Response.json({ ok: true, data: result })
})
```

### 5.3 Prompt sugerido para el LLM

```
You are an extractor of data from Colombian bank notifications.
Given the text of a notification, respond ONLY with JSON, no additional text, no markdown, no backticks:

{
  "type": "income" | "expense" | "transfer" | null,
  "amount": <number, no symbols or thousand separators>,
  "currency": "COP",
  "merchant": "<merchant or entity name for income/expense, or null>",
  "suggested_destination_account": "<account name or alias if it's a transfer between the user's own accounts, or null>",
  "confidence": "high" | "medium" | "low"
}

Use "transfer" only when the notification clearly indicates a movement between the user's own accounts. If it's a payment to a third party or merchant, use "expense". If it's money received from a third party, use "income". If the text does not clearly correspond to a financial transaction, respond with type null and confidence low.

Notification text:
"""
{text}
"""
```

> Nota: `suggested_destination_account` es solo un nombre/alias en texto libre (lo que el banco menciona en la notificación) — no es el UUID real. El cliente (app) es quien resuelve ese nombre contra las cuentas ya registradas del usuario, o lo deja pendiente en la bandeja de revisión si no logra hacer el match con ninguna cuenta existente.

### 5.4 Validación de la respuesta (obligatoria, no confiar ciegamente)

- `type` debe estar en el enum permitido
- `amount` debe ser numérico y mayor o igual a 0
- Si `type = 'transfer'`: la app debe lograr resolver `suggested_destination_account` contra una cuenta existente del usuario antes de guardar; si no encuentra coincidencia, cae en `review_queue` para que el usuario la asocie manualmente
- Si `confidence` es `"low"` → tratar como fallo y mandar a `review_queue`, aunque el JSON sea técnicamente válido

### 5.5 Deduplicación (relevante para notificaciones que llegan tarde desde la cola offline)

Antes de insertar una transacción proveniente de una notificación (en tiempo real o desde la cola offline sincronizada), el cliente consulta si ya existe una transacción manual con **monto, cuenta y fecha cercana coincidentes** (ventana sugerida: ±10 minutos). Si existe coincidencia, no se inserta una nueva fila.

---

## 6. Rate limiting

```sql
create table public.llm_usage (
    user_id uuid not null references auth.users(id),
    date date not null default current_date,
    calls int not null default 0,
    primary key (user_id, date)
);

create or replace function public.register_llm_usage(p_user_id uuid, p_limit int default 100)
returns boolean
language plpgsql
security definer set search_path = public
as $$
declare
  v_calls int;
begin
  insert into public.llm_usage (user_id, date, calls)
  values (p_user_id, current_date, 1)
  on conflict (user_id, date)
  do update set calls = public.llm_usage.calls + 1
  returning calls into v_calls;

  return v_calls <= p_limit;
end;
$$;

revoke all on function public.register_llm_usage(uuid, int) from public, anon, authenticated;
grant execute on function public.register_llm_usage(uuid, int) to service_role;
```
Protege contra abuso si el APK es decompilado y alguien extrae el endpoint (no la API key del LLM, que nunca sale del servidor).

---

## 7. Secrets y configuración

- API key del LLM: se configura como secret de Supabase (`GEMINI_API_KEY`) desde el dashboard, obtenida gratis en Google AI Studio — nunca en el código ni en el cliente
- `GEMINI_MODEL`: secret opcional para definir el modelo (default `gemini-2.0-flash`) (tiene valor por defecto en el código)
- Variables de entorno separadas para desarrollo/producción si se usan branches de Supabase

---

## 8. Autenticación

- **Supabase Auth** con dos métodos habilitados: **email/password** y **Google (OAuth)**
- El JWT emitido por Supabase Auth se guarda en el cliente Android (ver documento de Frontend) y se envía en cada llamada a la Edge Function y a las tablas vía PostgREST
- El signup por email debe enviar `first_name` y `last_name` en `data` (metadata), para que el trigger cree el registro en `users`; con Google, esos campos los provee automáticamente el perfil de la cuenta de Google
- **Configuración pendiente en el dashboard** (Authentication → Providers → Google): activar el proveedor y cargar el Client ID + Client Secret generados en Google Cloud Console (ver detalle de esa configuración en el documento de Frontend, sección 3.3)

---

## 9. Checklist de seguridad backend

- [x] RLS activo y probado en las 7 tablas
- [x] Edge Function rechaza requests sin JWT válido
- [x] Rate limiting implementado (RPC atómico + rechazo con 429)
- [x] Secrets no versionados en git, solo en Supabase (`GEMINI_API_KEY` pendiente de configurar por el usuario)
- [x] Advisor de seguridad de Supabase: 0 hallazgos
- [ ] Logs de la Edge Function no deben imprimir el texto completo de notificaciones en claro en producción (revisar antes de lanzar)

---

## 10. Estado de implementación

**Ya desplegado y funcional** en el proyecto Supabase real (`app-finanzas`, región `sa-east-1`):
- Las 7 tablas, con RLS, constraints e índices
- El trigger `on_auth_user_created` → `create_user_profile()`
- La función `register_llm_usage()`
- La Edge Function `parse-notification`, versión activa

**Pendiente (fuera del alcance de lo que se puede hacer sin intervención manual del usuario):**
- Configurar el secret `GEMINI_API_KEY` en el dashboard de Supabase (obtenerla gratis en Google AI Studio)
- El proveedor del LLM ya está definido: Gemini (tier gratuito). Cambiar de proveedor en el futuro solo requiere tocar la función `callLLM` dentro de la Edge Function
