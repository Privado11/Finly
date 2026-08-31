package co.privado.finly.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.data.local.SessionDataStore
import co.privado.finly.domain.repository.AuthRepository
import co.privado.finly.domain.repository.AccountRepository
import co.privado.finly.domain.repository.CategoryRepository
import co.privado.finly.domain.repository.TransactionRepository
import co.privado.finly.ui.state.GlobalMessageNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionDataStore,
    private val accountRepo: AccountRepository,
    private val categoryRepo: CategoryRepository,
    private val txRepo: TransactionRepository,
    private val globalMessageNotifier: GlobalMessageNotifier,
    private val notificadorApp: co.privado.finly.service.NotificadorApp
) : ViewModel() {

    val userName: StateFlow<String> = sessionStore.userDisplayState
        .map { it.first }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Usuario")
        
    val userEmail: StateFlow<String> = sessionStore.userDisplayState
        .map { it.second }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Sin correo")

    val biometricEnabled: StateFlow<Boolean> = sessionStore.biometricEnabledState
    val appNotificationsEnabled: StateFlow<Boolean> = sessionStore.appNotificationsState

    fun updateName(firstName: String, lastName: String) {
        viewModelScope.launch {
            val email = userEmail.value
            sessionStore.guardarDatosUsuario(firstName, lastName, email)
            
            globalMessageNotifier.showMessage("Nombre actualizado exitosamente")

            val currentId = sessionStore.getUserId() ?: authRepository.currentUserId()
            if (currentId != null) {
                authRepository.updateUserMetadata(firstName, lastName)
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            sessionStore.setBiometricEnabled(enabled)
            val estado = if (enabled) "activado" else "desactivado"
            globalMessageNotifier.showMessage("Desbloqueo con huella $estado")
        }
    }

    fun testNotification() {
        viewModelScope.launch {
            notificadorApp.ingresoRegistrado("15,000", "Nómina", "test-tx-id")
            kotlinx.coroutines.delay(500)
            notificadorApp.egresoRegistrado("2,500", "Supermercado", "test-tx-id-2")
            kotlinx.coroutines.delay(500)
            notificadorApp.requiereVerificacionManual()
            kotlinx.coroutines.delay(500)
            notificadorApp.guardadoEnColaOffline()
        }
    }

    fun setAppNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            sessionStore.setAppNotificationsEnabled(enabled)
            val estado = if (enabled) "activadas" else "desactivadas"
            globalMessageNotifier.showMessage("Notificaciones de la app $estado")
        }
    }

    fun logout() {
        viewModelScope.launch {
            accountRepo.clearCache()
            categoryRepo.clearCache()
            txRepo.clearCache()
            authRepository.signOut()
        }
    }
}
