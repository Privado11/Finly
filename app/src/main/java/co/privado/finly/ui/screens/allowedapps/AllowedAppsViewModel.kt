package co.privado.finly.ui.screens.allowedapps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.repository.WhitelistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstalledApp(val packageName: String, val label: String, val icon: android.graphics.drawable.Drawable)
data class AllowedAppsUiState(val apps: List<InstalledApp> = emptyList(), val allowedPackages: Set<String> = emptySet(), val isLoading: Boolean = true, val isUpdating: Set<String> = emptySet(), val error: String? = null, val listenerEnabled: Boolean = false)

@HiltViewModel
class AllowedAppsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val whitelistRepository: WhitelistRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AllowedAppsUiState())
    val uiState: StateFlow<AllowedAppsUiState> = _uiState.asStateFlow()
    init { load() }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null, listenerEnabled = listenerIsEnabled()) }
        val whitelist = whitelistRepository.getWhitelist()
        whitelist.onSuccess { allowed ->
            _uiState.update { it.copy(apps = installedApps(), allowedPackages = allowed.filter { item -> item.active }.map { item -> item.packageName }.toSet(), isLoading = false, listenerEnabled = listenerIsEnabled()) }
        }.onFailure { _uiState.update { it.copy(isLoading = false, error = "No pudimos cargar las apps permitidas. Inténtalo de nuevo.") } }
    }

    fun toggle(app: InstalledApp, enabled: Boolean) = viewModelScope.launch {
        _uiState.update { it.copy(isUpdating = it.isUpdating + app.packageName, error = null) }
        whitelistRepository.setAllowed(app.packageName, app.label, enabled)
            .onSuccess { _uiState.update { state -> state.copy(allowedPackages = if (enabled) state.allowedPackages + app.packageName else state.allowedPackages - app.packageName, isUpdating = state.isUpdating - app.packageName) } }
            .onFailure { _uiState.update { state -> state.copy(isUpdating = state.isUpdating - app.packageName, error = "No pudimos guardar el cambio de ${app.label}.") } }
    }

    fun refreshPermission() = _uiState.update { it.copy(listenerEnabled = listenerIsEnabled()) }
    fun dismissError() = _uiState.update { it.copy(error = null) }

    private fun listenerIsEnabled() = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    private fun installedApps(): List<InstalledApp> = context.packageManager.getInstalledApplications(0)
        .asSequence()
        .filter { app -> context.packageManager.getLaunchIntentForPackage(app.packageName) != null && app.packageName != context.packageName }
        .map { app -> InstalledApp(app.packageName, context.packageManager.getApplicationLabel(app).toString(), app.loadIcon(context.packageManager)) }
        .sortedBy { it.label.lowercase() }
        .toList()
}
