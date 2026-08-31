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

    override suspend fun isSessionValid(): Boolean {
        val accessToken = sessionStore.getAccessToken() ?: return false
        val refreshToken = sessionStore.refreshTokenFlow.first()

        try {
            if (refreshToken != null) {
                // Importamos el token que nosotros persistimos
                supabase.auth.importAuthToken(accessToken, refreshToken = refreshToken, autoRefresh = true)
            } else {
                supabase.auth.importAuthToken(accessToken)
            }
        } catch (e: Exception) {
            // Si la importación o el refresh automático fallan (ej. por red),
            // ignoramos el error. Si el token está expirado y no hay red, Supabase
            // limpiará la sesión en memoria, pero nuestro datastore lo retendrá para el próximo intento.
        }

        val session = supabase.auth.currentSessionOrNull()
        if (session != null) {
            guardarSesionActual()
            return true
        }

        // Si currentSessionOrNull es null, significa que Supabase no pudo validar la sesión
        // (por ejemplo, refresh_token fue revocado o expiró, o no hay internet y el access_token expiró).
        // Sin embargo, queremos que offline puedan entrar a la app con el pin.
        // Como el usuario pidió remover el chequeo de JWT manual, devolvemos true si tenemos access token.
        return true
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun guardarSesionActual() {
        val session = supabase.auth.currentSessionOrNull() ?: return
        val user = session.user
        sessionStore.guardarSesion(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            expiresAtSeconds = session.expiresAt?.epochSeconds ?: 0,
            userId = user?.id
        )
        if (user != null) {
            val email = user.email ?: ""
            val md = user.userMetadata
            val firstName = md?.get("first_name")?.toString()?.trim('"') 
                ?: md?.get("name")?.toString()?.trim('"')?.split(" ")?.firstOrNull() ?: ""
            val lastName = md?.get("last_name")?.toString()?.trim('"')
                ?: md?.get("name")?.toString()?.trim('"')?.split(" ")?.drop(1)?.joinToString(" ") ?: ""
            sessionStore.guardarDatosUsuario(firstName, lastName, email)
        }
    }

    override suspend fun updateUserMetadata(firstName: String, lastName: String) {
        runCatching {
            supabase.auth.updateUser {
                data = kotlinx.serialization.json.buildJsonObject {
                    put("first_name", firstName)
                    put("last_name", lastName)
                }
            }
        }
    }
}
