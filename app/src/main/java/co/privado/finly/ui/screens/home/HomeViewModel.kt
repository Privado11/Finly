package co.privado.finly.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.Transaction
import co.privado.finly.domain.model.TransactionType
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
import javax.inject.Inject

data class ExpenseSlice(val label: String, val amount: Double, val colorIndex: Int)
data class DailyExpense(val day: String, val amount: Double)

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val balance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val expenseSlices: List<ExpenseSlice> = emptyList(),
    val weeklyExpenses: List<DailyExpense> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionUpdateNotifier: TransactionUpdateNotifier
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var transactionsCache: List<Transaction> = emptyList()
    private var categoriesCache: List<Category> = emptyList()

    init {
        refresh()
        viewModelScope.launch {
            transactionUpdateNotifier.created.collect { transaction ->
                transactionsCache = (transactionsCache + transaction).distinctBy { it.id }
                _uiState.value = buildState(transactionsCache, categoriesCache)
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
            _uiState.value = buildState(list, categories)
        }
            .onFailure { _uiState.update { it.copy(isLoading = false, error = "No pudimos actualizar tu resumen. Desliza para reintentar.") } }
    }

    private fun buildState(transactions: List<Transaction>, categories: List<Category>): HomeUiState {
        val today = LocalDate.now()
        val currentMonth = transactions.filter { it.localDate()?.let { date -> date.year == today.year && date.month == today.month } == true }
        val income = currentMonth.filter { it.type == TransactionType.income }.sumOf { it.amount }
        val expense = currentMonth.filter { it.type == TransactionType.expense }.sumOf { it.amount }
        val balance = transactions.sumOf { transaction -> when (transaction.type) { TransactionType.income -> transaction.amount; TransactionType.expense -> -transaction.amount; TransactionType.transfer -> 0.0 } }
        val categoryNames = categories.associateBy(Category::id)
        val slices = currentMonth.filter { it.type == TransactionType.expense }.groupBy { categoryNames[it.categoryId]?.name ?: "Sin categoría" }.entries.mapIndexed { index, (name, entries) -> ExpenseSlice(name, entries.sumOf { it.amount }, index) }.sortedByDescending { it.amount }.take(4)
        val week = (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            DailyExpense(date.dayOfWeek.name.take(1), transactions.filter { it.type == TransactionType.expense && it.localDate() == date }.sumOf { it.amount })
        }
        return HomeUiState(false, balance = balance, monthlyIncome = income, monthlyExpense = expense, recentTransactions = transactions.sortedByDescending { it.date }.take(5), expenseSlices = slices, weeklyExpenses = week)
    }
}

private fun Transaction.localDate(): LocalDate? = runCatching { Instant.parse(date).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()
