package co.privado.finly.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.finlySessionStore by preferencesDataStore(name = "finly_session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val EXPIRES_AT = longPreferencesKey("expires_at_epoch_seconds")
        val USER_ID = stringPreferencesKey("user_id")
    }

    suspend fun guardarSesion(accessToken: String, refreshToken: String?, expiresAtSeconds: Long?, userId: String?) {
        context.finlySessionStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            if (refreshToken != null) prefs[Keys.REFRESH_TOKEN] = refreshToken else prefs.remove(Keys.REFRESH_TOKEN)
            if (expiresAtSeconds != null) prefs[Keys.EXPIRES_AT] = expiresAtSeconds else prefs.remove(Keys.EXPIRES_AT)
            if (userId != null) prefs[Keys.USER_ID] = userId else prefs.remove(Keys.USER_ID)
        }
    }

    suspend fun limpiarSesion() {
        context.finlySessionStore.edit { it.clear() }
    }

    val accessTokenFlow: Flow<String?> = context.finlySessionStore.data.map { it[Keys.ACCESS_TOKEN] }
    val refreshTokenFlow: Flow<String?> = context.finlySessionStore.data.map { it[Keys.REFRESH_TOKEN] }
    val expiresAtFlow: Flow<Long?> = context.finlySessionStore.data.map { it[Keys.EXPIRES_AT] }

    suspend fun isSessionValid(): Boolean {
        val prefs = context.finlySessionStore.data.first()
        val token = prefs[Keys.ACCESS_TOKEN] ?: return false
        val expiresAt = prefs[Keys.EXPIRES_AT] ?: return true
        return expiresAt * 1000 > System.currentTimeMillis()
    }

    suspend fun getAccessToken(): String? {
        return context.finlySessionStore.data.first()[Keys.ACCESS_TOKEN]
    }

    suspend fun getUserId(): String? {
        return context.finlySessionStore.data.first()[Keys.USER_ID]
    }
}
