package co.privado.finly.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.model.*
import co.privado.finly.domain.repository.AccountRepository
import co.privado.finly.domain.repository.CategoryRepository
import co.privado.finly.domain.repository.ReviewQueueRepository
import co.privado.finly.domain.repository.TransactionRepository
import co.privado.finly.ui.state.GlobalMessageNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ReviewUiState(
    val isLoading: Boolean = true,
    val pendingItems: List<ReviewQueueItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val accounts: List<AccountBalance> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewQueueRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val globalMessageNotifier: GlobalMessageNotifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val accounts = accountRepository.getAccounts().getOrDefault(emptyList())
            val categories = categoryRepository.getCategories().getOrDefault(emptyList())
            
            reviewRepository.getPending().fold(
                onSuccess = { items ->
                    _uiState.value = _uiState.value.copy(isLoading = false, pendingItems = items, accounts = accounts, categories = categories)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = err.message, accounts = accounts, categories = categories)
                }
            )
        }
    }

    fun markAsResolved(id: String, message: String) {
        viewModelScope.launch {
            reviewRepository.markResolved(id).fold(
                onSuccess = {
                    globalMessageNotifier.showMessage(message)
                    loadData()
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(error = err.message)
                }
            )
        }
    }

    fun saveTransactionAndResolve(
        reviewItemId: String,
        amount: Double,
        accountId: String,
        categoryId: String?,
        type: TransactionType,
        merchant: String?,
        description: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val transaction = Transaction(
                sourceAccountId = accountId,
                categoryId = categoryId,
                type = type,
                amount = amount,
                currency = "COP",
                merchant = merchant,
                description = description,
                source = TransactionSource.manual,
                date = Instant.now().toString()
            )
            transactionRepository.addTransaction(transaction).fold(
                onSuccess = {
                    markAsResolved(reviewItemId, "Transacción guardada exitosamente")
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = err.message)
                }
            )
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
