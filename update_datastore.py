import re

with open('app/src/main/java/co/privado/finly/data/local/SessionDataStore.kt', 'r') as f:
    content = f.read()

# Add key
content = content.replace(
    'val APP_NOTIFICATIONS = androidx.datastore.preferences.core.booleanPreferencesKey("app_notifications_enabled")',
    'val APP_NOTIFICATIONS = androidx.datastore.preferences.core.booleanPreferencesKey("app_notifications_enabled")\n        val BALANCES_VISIBLE = androidx.datastore.preferences.core.booleanPreferencesKey("balances_visible")'
)

# Add state and setter
insert_logic = """
    val balancesVisibleState: StateFlow<Boolean> = context.finlySessionStore.data.map { it[Keys.BALANCES_VISIBLE] ?: true }.stateIn(scope, SharingStarted.Eagerly, true)

    suspend fun setBalancesVisible(visible: Boolean) {
        context.finlySessionStore.edit { it[Keys.BALANCES_VISIBLE] = visible }
    }

    suspend fun guardarSesion"""
content = content.replace('    suspend fun guardarSesion', insert_logic.lstrip('\n'))

with open('app/src/main/java/co/privado/finly/data/local/SessionDataStore.kt', 'w') as f:
    f.write(content)
