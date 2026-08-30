package co.privado.finly.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import co.privado.finly.domain.model.AllowedApp
import co.privado.finly.domain.repository.WhitelistRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.whitelistDataStore by preferencesDataStore(name = "finly_whitelist")

@Singleton
class WhitelistRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WhitelistRepository {
    
    private val ALLOWED_APPS_KEY = stringSetPreferencesKey("allowed_apps_set")

    override suspend fun isAllowed(packageName: String): Boolean {
        return try {
            val prefs = context.whitelistDataStore.data.first()
            val allowedApps = prefs[ALLOWED_APPS_KEY] ?: emptySet()
            allowedApps.contains(packageName)
        } catch (_: Exception) { false }
    }

    override suspend fun getWhitelist(): Result<List<AllowedApp>> = runCatching {
        val prefs = context.whitelistDataStore.data.first()
        val allowedApps = prefs[ALLOWED_APPS_KEY] ?: emptySet()
        allowedApps.map { pkg -> 
            AllowedApp(packageName = pkg, active = true) 
        }
    }

    override suspend fun setAllowed(packageName: String, displayName: String, active: Boolean): Result<Unit> = runCatching {
        context.whitelistDataStore.edit { prefs ->
            val current = prefs[ALLOWED_APPS_KEY]?.toMutableSet() ?: mutableSetOf()
            if (active) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            prefs[ALLOWED_APPS_KEY] = current
        }
        Unit
    }

    override suspend fun remove(packageName: String): Result<Unit> = runCatching {
        context.whitelistDataStore.edit { prefs ->
            val current = prefs[ALLOWED_APPS_KEY]?.toMutableSet() ?: mutableSetOf()
            current.remove(packageName)
            prefs[ALLOWED_APPS_KEY] = current
        }
        Unit
    }
}
