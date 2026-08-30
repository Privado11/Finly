package co.privado.finly.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.model.Account
import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.Transaction
import co.privado.finly.domain.model.TransactionSource
import co.privado.finly.domain.model.TransactionType
import co.privado.finly.domain.repository.AccountRepository
import co.privado.finly.domain.repository.CategoryRepository
import co.privado.finly.domain.repository.TransactionRepository
import co.privado.finly.ui.state.TransactionUpdateNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class TransactionsUiState(val accounts: List<Account> = emptyList(), val categories: List<Category> = emptyList(), val isLoading: Boolean = true, val isSaving: Boolean = false, val error: String? = null)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionUpdateNotifier: TransactionUpdateNotifier
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()
    init { loadFormData() }
    private fun loadFormData() = viewModelScope.launch {
        val accounts = accountRepository.getAccounts().getOrElse { emptyList() }
        val categories = categoryRepository.getCategories().getOrElse { emptyList() }
        _uiState.update { it.copy(accounts = accounts, categories = categories, isLoading = false, error = if (accounts.isEmpty()) "Primero crea una cuenta para registrar un movimiento." else null) }
    }
    fun save(amount: String, type: TransactionType, sourceId: String?, destinationId: String?, categoryId: String?, merchant: String, onSuccess: () -> Unit) {
        val value = amount.replace(",", ".").toDoubleOrNull()
        when { value == null || value <= 0 -> _uiState.update { it.copy(error = "Ingresa un monto válido.") }; sourceId == null -> _uiState.update { it.copy(error = "Selecciona una cuenta.") }; type == TransactionType.transfer && (destinationId == null || destinationId == sourceId) -> _uiState.update { it.copy(error = "Elige una cuenta destino distinta.") }; else -> viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            transactionRepository.addTransaction(Transaction(sourceAccountId = sourceId, destinationAccountId = if (type == TransactionType.transfer) destinationId else null, categoryId = if (type == TransactionType.transfer) null else categoryId, type = type, amount = value, merchant = merchant.trim().ifBlank { null }, source = TransactionSource.manual, date = Instant.now().toString()))
                .onSuccess { transaction ->
                    transactionUpdateNotifier.notifyCreated(transaction)
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    android.util.Log.e("TransactionsViewModel", "Error saving transaction", error)
                    _uiState.update { it.copy(isSaving = false, error = "No fue posible guardar el movimiento. Inténtalo de nuevo.") } 
                }
        } }
    }
    fun dismissError() = _uiState.update { it.copy(error = null) }
}
