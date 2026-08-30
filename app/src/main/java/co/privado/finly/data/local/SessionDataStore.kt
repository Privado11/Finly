package co.privado.finly.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

private val Context.finlySessionStore by preferencesDataStore(name = "finly_session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val EXPIRES_AT = longPreferencesKey("expires_at_epoch_seconds")
        val USER_ID = stringPreferencesKey("user_id")
        val FIRST_NAME = stringPreferencesKey("first_name")
        val LAST_NAME = stringPreferencesKey("last_name")
        val EMAIL = stringPreferencesKey("email")
        val BIOMETRIC_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("biometric_enabled")
    }

    val accessTokenFlow: Flow<String?> = context.finlySessionStore.data.map { it[Keys.ACCESS_TOKEN] }
    val refreshTokenFlow: Flow<String?> = context.finlySessionStore.data.map { it[Keys.REFRESH_TOKEN] }
    val expiresAtFlow: Flow<Long?> = context.finlySessionStore.data.map { it[Keys.EXPIRES_AT] }

    val firstNameFlow: Flow<String?> = context.finlySessionStore.data.map { it[Keys.FIRST_NAME] }
    val lastNameFlow: Flow<String?> = context.finlySessionStore.data.map { it[Keys.LAST_NAME] }
    val emailFlow: Flow<String?> = context.finlySessionStore.data.map { it[Keys.EMAIL] }

    val biometricEnabledState: StateFlow<Boolean> = context.finlySessionStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: true }.stateIn(scope, SharingStarted.Eagerly, true)

    val userDisplayState: StateFlow<Pair<String, String>> = combine(
        firstNameFlow,
        lastNameFlow,
        emailFlow
    ) { first, last, email ->
        val name = "${first.orEmpty()} ${last.orEmpty()}".trim()
        Pair(name.ifEmpty { "Usuario" }, email ?: "Sin correo")
    }.stateIn(scope, SharingStarted.Eagerly, Pair("Usuario", "Sin correo"))

    suspend fun guardarSesion(accessToken: String, refreshToken: String?, expiresAtSeconds: Long?, userId: String?) {
        context.finlySessionStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            if (refreshToken != null) prefs[Keys.REFRESH_TOKEN] = refreshToken else prefs.remove(Keys.REFRESH_TOKEN)
            if (expiresAtSeconds != null) prefs[Keys.EXPIRES_AT] = expiresAtSeconds else prefs.remove(Keys.EXPIRES_AT)
            if (userId != null) prefs[Keys.USER_ID] = userId else prefs.remove(Keys.USER_ID)
        }
    }

    suspend fun guardarDatosUsuario(firstName: String, lastName: String, email: String) {
        context.finlySessionStore.edit { prefs ->
            prefs[Keys.FIRST_NAME] = firstName
            prefs[Keys.LAST_NAME] = lastName
            prefs[Keys.EMAIL] = email
        }
    }

    suspend fun limpiarSesion() {
        context.finlySessionStore.edit { it.clear() }
    }

    suspend fun isSessionValid(): Boolean {
        val prefs = context.finlySessionStore.data.first()
        val token = prefs[Keys.ACCESS_TOKEN] ?: return false
        return true
    }

    suspend fun getAccessToken(): String? {
        return context.finlySessionStore.data.first()[Keys.ACCESS_TOKEN]
    }

    suspend fun isBiometricEnabled(): Boolean {
        return context.finlySessionStore.data.first()[Keys.BIOMETRIC_ENABLED] ?: true
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.finlySessionStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun getUserId(): String? {
        return context.finlySessionStore.data.first()[Keys.USER_ID]
    }
}
