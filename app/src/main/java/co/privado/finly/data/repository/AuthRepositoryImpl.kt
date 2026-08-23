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
        // Flujo nativo con Credential Manager: se usa el provider IDToken, no Google (ese es para OAuth con navegador).
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

    override suspend fun isSessionValid(): Boolean {
        val session = supabase.auth.currentSessionOrNull() ?: return false
        return try {
            supabase.auth.currentUserOrNull() != null
        } catch (_: Exception) { false }
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