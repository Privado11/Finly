package co.privado.finly.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.model.Account
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
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val accountToDelete: Account? = null
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val globalMessageNotifier: GlobalMessageNotifier
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    init { loadAccounts() }

    fun loadAccounts() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        accountRepository.getAccounts()
            .onSuccess { accounts -> _uiState.update { it.copy(accounts = accounts, isLoading = false) } }
            .onFailure { error -> _uiState.update { it.copy(isLoading = false, error = readableError(error)) } }
    }

    fun showCreateDialog(show: Boolean) = _uiState.update { it.copy(showCreateDialog = show, error = null) }
    
    fun showDeleteDialog(account: Account?) = _uiState.update { it.copy(accountToDelete = account, error = null) }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            accountRepository.deleteAccount(account.id)
                .onSuccess { 
                    _uiState.update { state -> 
                        state.copy(
                            accounts = state.accounts.filter { it.id != account.id }, 
                            isSaving = false, 
                            accountToDelete = null 
                        ) 
                    }
                    globalMessageNotifier.showMessage("Cuenta eliminada")
                }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, error = readableError(error), accountToDelete = null) } }
        }
    }

    fun createAccount(name: String, type: AccountType) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Escribe un nombre para la cuenta.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            accountRepository.addAccount(name, type)
                .onSuccess { created ->
                    _uiState.update {
                        it.copy(
                            accounts = (it.accounts + created).sortedBy { account -> account.name.lowercase() },
                            isSaving = false,
                            showCreateDialog = false
                        )
                    }
                    globalMessageNotifier.showMessage("Cuenta creada")
                }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, error = readableError(error)) } }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }

    private fun readableError(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("network", ignoreCase = true) || message.contains("resolve", ignoreCase = true) -> "No pudimos conectar con Finly. Revisa tu conexión."
            message.contains("sesión expiró", ignoreCase = true) -> message
            message.contains("foreign key", ignoreCase = true) || message.contains("violates", ignoreCase = true) -> "No puedes eliminar esta cuenta porque tiene movimientos registrados."
            else -> "No fue posible completar la acción. Inténtalo de nuevo."
        }
    }
}
