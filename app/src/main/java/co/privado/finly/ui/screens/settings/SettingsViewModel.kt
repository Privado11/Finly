package co.privado.finly.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.data.local.SessionDataStore
import co.privado.finly.domain.repository.AuthRepository
import co.privado.finly.domain.repository.AccountRepository
import co.privado.finly.domain.repository.CategoryRepository
import co.privado.finly.domain.repository.TransactionRepository
import co.privado.finly.ui.state.GlobalMessageNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class UserRow(
    val id: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val email: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val supabase: SupabaseClient,
    private val sessionDataStore: SessionDataStore,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val globalMessageNotifier: GlobalMessageNotifier
) : ViewModel() {

    val userName: StateFlow<String> = sessionDataStore.userDisplayState
        .map { it.first }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sessionDataStore.userDisplayState.value.first)

    val userEmail: StateFlow<String> = sessionDataStore.userDisplayState
        .map { it.second }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sessionDataStore.userDisplayState.value.second)

    val biometricEnabled: StateFlow<Boolean> = sessionDataStore.biometricEnabledState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sessionDataStore.biometricEnabledState.value)

    init {
        fetchFreshData()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            sessionDataStore.setBiometricEnabled(enabled)
            if (enabled) {
                globalMessageNotifier.showMessage("Desbloqueo con huella activado")
            } else {
                globalMessageNotifier.showMessage("Desbloqueo con huella desactivado")
            }
        }
    }

    private fun fetchFreshData() {
        viewModelScope.launch {
            try {
                val userId = sessionDataStore.getUserId() ?: return@launch
                
                val userRow = supabase.postgrest["users"]
                    .select {
                        filter { eq("id", userId) }
                    }.decodeSingleOrNull<UserRow>()
                
                if (userRow != null) {
                    sessionDataStore.guardarDatosUsuario(
                        firstName = userRow.firstName.orEmpty(),
                        lastName = userRow.lastName.orEmpty(),
                        email = userRow.email.orEmpty()
                    )
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error fetching user", e)
            }
        }
    }

    fun updateName(firstName: String, lastName: String) {
        viewModelScope.launch {
            try {
                val userId = sessionDataStore.getUserId() ?: return@launch
                
                // Optimistic UI update (saves to local first)
                val currentEmail = sessionDataStore.userDisplayState.value.second
                sessionDataStore.guardarDatosUsuario(firstName, lastName, currentEmail)
                
                // Send to Supabase
                supabase.postgrest["users"].update(
                    {
                        set("first_name", firstName)
                        set("last_name", lastName)
                    }
                ) {
                    filter { eq("id", userId) }
                }
                globalMessageNotifier.showMessage("Nombre actualizado correctamente")
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error updating name", e)
                globalMessageNotifier.showMessage("Error al actualizar el nombre")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            sessionDataStore.limpiarSesion()
            accountRepository.clearCache()
            categoryRepository.clearCache()
            transactionRepository.clearCache()
        }
    }
}
