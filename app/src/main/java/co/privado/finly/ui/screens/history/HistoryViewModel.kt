package co.privado.finly.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.Transaction
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

import co.privado.finly.domain.model.TransactionType

enum class TransactionFilter {
    ALL, INCOME, EXPENSE, TRANSFER
}

data class HistoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val groupedTransactions: Map<String, List<Transaction>> = emptyMap(),
    val categoryNames: Map<String, String> = emptyMap(),
    val filter: TransactionFilter = TransactionFilter.ALL
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionUpdateNotifier: TransactionUpdateNotifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var transactionsCache: List<Transaction> = emptyList()
    private var categoriesCache: List<Category> = emptyList()

    init {
        refresh()
        viewModelScope.launch {
            transactionUpdateNotifier.created.collect { transaction ->
                transactionsCache = (transactionsCache + transaction).distinctBy { it.id }
                _uiState.value = buildState(transactionsCache, categoriesCache, _uiState.value.filter)
            }
        }
        viewModelScope.launch {
            transactionUpdateNotifier.deleted.collect { transactionId ->
                transactionsCache = transactionsCache.filter { it.id != transactionId }
                _uiState.value = buildState(transactionsCache, categoriesCache, _uiState.value.filter)
            }
        }
    }

    fun refresh() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val transactions = transactionRepository.getTransactions()
        val categories = categoryRepository.getCategories().getOrDefault(emptyList())

        transactions.onSuccess { list ->
            transactionsCache = list
            categoriesCache = categories
            _uiState.value = buildState(list, categories, _uiState.value.filter)
        }.onFailure {
            _uiState.update { state -> state.copy(isLoading = false, error = "No pudimos cargar tus movimientos.") }
        }
    }

    fun setFilter(filter: TransactionFilter) {
        _uiState.value = buildState(transactionsCache, categoriesCache, filter)
    }

    private fun buildState(transactions: List<Transaction>, categories: List<Category>, filter: TransactionFilter): HistoryUiState {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "ES"))
        
        val filtered = when (filter) {
            TransactionFilter.ALL -> transactions
            TransactionFilter.INCOME -> transactions.filter { it.type == TransactionType.income }
            TransactionFilter.EXPENSE -> transactions.filter { it.type == TransactionType.expense }
            TransactionFilter.TRANSFER -> transactions.filter { it.type == TransactionType.transfer }
        }
        
        val sorted = filtered.sortedByDescending { it.date }
        
        val grouped = sorted.groupBy { tx ->
            val txDate = tx.localDate()
            val formattedDate = txDate?.format(dateFormatter)?.replaceFirstChar { it.uppercase() } ?: ""
            when (txDate) {
                today -> "Hoy • $formattedDate"
                yesterday -> "Ayer • $formattedDate"
                null -> "Desconocido"
                else -> formattedDate
            }
        }
        
        val categoryNames = categories.associate { it.id!! to it.name }
        
        return HistoryUiState(
            isLoading = false,
            groupedTransactions = grouped,
            categoryNames = categoryNames,
            filter = filter
        )
    }
}

private fun Transaction.localDate(): LocalDate? = runCatching { 
    Instant.parse(date).atZone(ZoneId.systemDefault()).toLocalDate() 
}.getOrNull()
