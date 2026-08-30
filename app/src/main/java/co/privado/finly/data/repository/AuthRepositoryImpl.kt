package co.privado.finly.data.repository

import co.privado.finly.data.local.SessionDataStore
import co.privado.finly.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionStore: SessionDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        guardarSesionActual()
    }

    override suspend fun signUp(email: String, password: String, firstName: String, lastName: String): Result<Unit> = runCatching {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("first_name", firstName)
                put("last_name", lastName)
            }
        }
        guardarSesionActual()
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        supabase.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
        }
        guardarSesionActual()
    }

    override suspend fun signOut() {
        runCatching { supabase.auth.signOut() }
        sessionStore.limpiarSesion()
    }

    override suspend fun currentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    /**
     * Valida la sesión guardada localmente sin hacer una llamada de red innecesaria.
     *
     * Flujo:
     * 1. Sin token local → false (login requerido).
     * 2. Token local no expirado → importar en memoria y retornar true (SIN llamada de red).
     * 3. Token expirado pero hay refresh token → intentar refresh con Supabase (requiere red).
     *    - Éxito → guardar nuevos tokens → true.
     *    - Fallo → false (login requerido).
     *
     * NUNCA llama a currentUserOrNull() solo para validar — eso haría una llamada de red
     * en cada startup y mandaría al usuario a login si no hay internet o Supabase tarda.
     */
    override suspend fun isSessionValid(): Boolean {
        val accessToken = sessionStore.getAccessToken() ?: return false

        // Caso 1: token local vigente — confiar en expires_at guardado, sin red
        if (sessionStore.isSessionValid()) {
            runCatching { supabase.auth.importAuthToken(accessToken) }
            return true
        }

        // Caso 2: token expirado — intentar renovar con refresh token (requiere red)
        val refreshToken = sessionStore.refreshTokenFlow.first()
        if (refreshToken != null) {
            return try {
                supabase.auth.importAuthToken(accessToken)
                supabase.auth.refreshCurrentSession()
                guardarSesionActual()
                true
            } catch (e: Exception) {
                false
            }
        }

        return false
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun guardarSesionActual() {
        val session = supabase.auth.currentSessionOrNull()
            ?: throw IllegalStateException("Sin sesión tras autenticación")
        sessionStore.guardarSesion(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            expiresAtSeconds = session.expiresAt.epochSeconds,
            userId = supabase.auth.currentUserOrNull()?.id
        )
    }
}
