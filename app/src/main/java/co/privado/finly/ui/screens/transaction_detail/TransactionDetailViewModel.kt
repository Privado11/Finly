package co.privado.finly.ui.screens.transaction_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.model.Transaction
import co.privado.finly.domain.repository.AccountRepository
import co.privado.finly.domain.repository.CategoryRepository
import co.privado.finly.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val transaction: Transaction? = null,
    val sourceAccountName: String = "",
    val destinationAccountName: String = "",
    val categoryName: String = "",
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionUpdateNotifier: co.privado.finly.ui.state.TransactionUpdateNotifier
) : ViewModel() {

    private val transactionId: String = checkNotNull(savedStateHandle["transactionId"])
    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    init {
        loadTransaction()
    }

    private fun loadTransaction() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        val txResult = transactionRepository.getTransactionById(transactionId)
        val accountsResult = accountRepository.getAccounts()
        val categoriesResult = categoryRepository.getCategories()
        
        txResult.onSuccess { tx ->
            val accounts = accountsResult.getOrDefault(emptyList())
            val categories = categoriesResult.getOrDefault(emptyList())
            
            val sourceName = accounts.find { it.id == tx.sourceAccountId }?.name ?: "Cuenta desconocida"
            val destName = accounts.find { it.id == tx.destinationAccountId }?.name ?: ""
            val catName = categories.find { it.id == tx.categoryId }?.name ?: "Sin categoría"
            
            _uiState.value = TransactionDetailUiState(
                isLoading = false,
                transaction = tx,
                sourceAccountName = sourceName,
                destinationAccountName = destName,
                categoryName = catName
            )
        }.onFailure {
            _uiState.update { it.copy(isLoading = false, error = "No pudimos cargar los detalles del movimiento.") }
        }
    }
    
    fun deleteTransaction() = viewModelScope.launch {
        _uiState.update { it.copy(isDeleting = true, error = null) }
        transactionRepository.deleteTransaction(transactionId)
            .onSuccess {
                transactionUpdateNotifier.notifyDeleted(transactionId)
                _uiState.update { it.copy(isDeleting = false, isDeleted = true) }
            }
            .onFailure {
                _uiState.update { it.copy(isDeleting = false, error = "No fue posible eliminar el movimiento.") }
            }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
