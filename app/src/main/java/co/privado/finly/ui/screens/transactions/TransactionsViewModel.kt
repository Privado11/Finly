package co.privado.finly.ui.screens.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.model.AccountBalance
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

data class TransactionsUiState(val accounts: List<AccountBalance> = emptyList(), val categories: List<Category> = emptyList(), val isLoading: Boolean = true, val isSaving: Boolean = false, val error: String? = null, val initialTransaction: Transaction? = null)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionUpdateNotifier: TransactionUpdateNotifier
) : ViewModel() {
    val transactionId: String? = savedStateHandle["transactionId"]
    
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()
    
    init { loadFormData() }
    
    private fun loadFormData() = viewModelScope.launch {
        val accounts = accountRepository.getAccounts().getOrElse { emptyList() }
        val categories = categoryRepository.getCategories().getOrElse { emptyList() }
        val initialTx = if (transactionId != null) transactionRepository.getTransactionById(transactionId).getOrNull() else null
        
        _uiState.update { it.copy(accounts = accounts, categories = categories, isLoading = false, error = if (accounts.isEmpty()) "Primero crea una cuenta para registrar un movimiento." else null, initialTransaction = initialTx) }
    }
    
    fun save(amount: String, type: TransactionType, sourceId: String?, destinationId: String?, categoryId: String?, merchant: String, notes: String?, onSuccess: () -> Unit) {
        val value = amount.replace(",", ".").toDoubleOrNull()
        
        val sourceAccount = _uiState.value.accounts.find { it.id == sourceId }
        val isOverdraft = (type == TransactionType.expense) && 
                          sourceAccount != null && value != null && value > sourceAccount.balance
                          
        when { 
            value == null || value <= 0 -> _uiState.update { it.copy(error = "Ingresa un monto válido.") }
            sourceId == null -> _uiState.update { it.copy(error = "Selecciona una cuenta.") }
            categoryId == null -> _uiState.update { it.copy(error = "Selecciona una categoría.") }
            isOverdraft && transactionId == null -> {
                val formattedBalance = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "CO")).apply { maximumFractionDigits = 0 }.format(sourceAccount?.balance ?: 0.0)
                _uiState.update { it.copy(error = "Saldo insuficiente en la cuenta. Disponible: $formattedBalance") }
            }
            else -> viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, error = null) }
                val txDate = _uiState.value.initialTransaction?.date ?: Instant.now().toString()
                val txSource = _uiState.value.initialTransaction?.source ?: TransactionSource.manual
                val newTx = Transaction(id = transactionId ?: "", sourceAccountId = sourceId, destinationAccountId = null, categoryId = categoryId, type = type, amount = value, merchant = merchant.trim().ifBlank { null }, source = txSource, date = txDate)
                
                val result = if (transactionId != null) {
                    transactionRepository.updateTransaction(transactionId, newTx)
                } else {
                    transactionRepository.addTransaction(newTx)
                }
                
                result.onSuccess { transaction ->
                    transactionUpdateNotifier.notifyCreated(transaction)
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                }.onFailure { error ->
                    android.util.Log.e("TransactionsViewModel", "Error saving transaction", error)
                    val msg = error.message ?: ""
                    val displayError = when {
                        msg.contains("Saldo insuficiente", ignoreCase = true) || msg.contains("P0001", ignoreCase = true) -> {
                            val match = Regex("Disponible (\\d+(\\.\\d+)?)").find(msg)
                            if (match != null) "Saldo insuficiente en la cuenta. Disponible: $${match.groupValues[1]}" else "Saldo insuficiente en la cuenta para realizar esta operación."
                        }
                        else -> "No fue posible guardar el movimiento. Inténtalo de nuevo."
                    }
                    _uiState.update { it.copy(isSaving = false, error = displayError) } 
                }
            } 
        }
    }
    fun dismissError() = _uiState.update { it.copy(error = null) }
}
