package co.privado.finly.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.model.AccountBalance
import co.privado.finly.domain.model.AccountType
import co.privado.finly.domain.repository.AccountRepository
import co.privado.finly.ui.state.GlobalMessageNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val accounts: List<AccountBalance> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val accountToDelete: AccountBalance? = null,
    val accountToEdit: AccountBalance? = null
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val globalMessageNotifier: GlobalMessageNotifier
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            accountRepository.observeAccounts().collect { accounts ->
                if (accounts.isNotEmpty() || !_uiState.value.isLoading) {
                    _uiState.update { it.copy(accounts = accounts, isLoading = false) }
                }
            }
        }
        loadAccounts() 
    }

    fun loadAccounts() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        accountRepository.getAccounts(forceRefresh = true)
            .onSuccess { accounts -> 
                _uiState.update { it.copy(accounts = accounts, isLoading = false) } 
            }
            .onFailure { error -> 
                _uiState.update { it.copy(isLoading = false, error = readableError(error)) } 
            }
    }

    fun showCreateDialog(show: Boolean) = _uiState.update { it.copy(showCreateDialog = show, error = null, accountToEdit = null) }
    
    fun showEditDialog(account: AccountBalance?) = _uiState.update { it.copy(accountToEdit = account, showCreateDialog = true, error = null) }
    
    fun showDeleteDialog(account: AccountBalance?) = _uiState.update { it.copy(accountToDelete = account, error = null) }

    fun deleteAccount(account: AccountBalance) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            accountRepository.deleteAccount(account.id)
                .onSuccess { 
                    _uiState.update { it.copy(isSaving = false, accountToDelete = null) }
                    globalMessageNotifier.showMessage("Cuenta eliminada")
                }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, error = readableError(error), accountToDelete = null) } }
        }
    }

    fun saveAccount(id: String?, name: String, type: AccountType, openingBalance: Double = 0.0) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Escribe un nombre para la cuenta.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = if (id == null) {
                accountRepository.addAccount(name, type, openingBalance)
            } else {
                accountRepository.updateAccount(id, name, type, openingBalance)
            }
            
            result.onSuccess {
                _uiState.update { it.copy(isSaving = false, showCreateDialog = false, accountToEdit = null) }
                globalMessageNotifier.showMessage(if (id == null) "Cuenta creada" else "Cuenta actualizada")
            }.onFailure { error -> 
                _uiState.update { it.copy(isSaving = false, error = readableError(error)) }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }

    private fun readableError(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("network", ignoreCase = true) || message.contains("resolve", ignoreCase = true) -> "No pudimos conectar con Finly. Revisa tu conexión."
            message.contains("sesión expiró", ignoreCase = true) -> message
            message.contains("23514") -> "El saldo inicial no puede ser negativo para este tipo de cuenta."
            message.contains("foreign key", ignoreCase = true) || message.contains("violates", ignoreCase = true) -> "No puedes eliminar esta cuenta porque tiene movimientos registrados."
            else -> "No fue posible completar la acción. Inténtalo de nuevo."
        }
    }
}
