package co.privado.finly.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import co.privado.finly.domain.model.Transaction
import co.privado.finly.domain.model.TransactionType
import co.privado.finly.ui.navigation.Routes
import java.text.NumberFormat
import java.util.Locale

private val ChartColors = listOf(Color(0xFF006C51), Color(0xFF386A95), Color(0xFF8E4F00), Color(0xFF765285))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = { TopAppBar(title = { Column { Text("Finly", style = MaterialTheme.typography.titleLarge); Text("Tu resumen financiero", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }, actions = { IconButton(onClick = viewModel::refresh) { Icon(Icons.Filled.Refresh, "Actualizar resumen") } }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { nav.navigate(Routes.Transactions) }, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Movimiento") }) }
    ) { padding ->
        if (state.isLoading) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else DashboardContent(state, Modifier.fillMaxSize().padding(padding), nav)
    }
}

@Composable
private fun DashboardContent(state: HomeUiState, modifier: Modifier, nav: NavController) {
    LazyColumn(modifier, contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        state.error?.let { item { ErrorBanner(it) } }
        item { BalanceCard(state.balance, state.monthlyIncome, state.monthlyExpense) }
        item { QuickActions(nav) }
        if (state.expenseSlices.isNotEmpty()) item { ExpensesCard(state.expenseSlices) }
        if (state.weeklyExpenses.any { it.amount > 0 }) item { WeeklyChart(state.weeklyExpenses) }
        item { Text("Últimos movimientos", style = MaterialTheme.typography.titleLarge) }
        if (state.recentTransactions.isEmpty()) item { EmptyMovement() }
        else items(state.recentTransactions.size) { TransactionRow(state.recentTransactions[it]) }
    }
}

@Composable private fun BalanceCard(balance: Double, income: Double, expense: Double) {
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Balance total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(6.dp)); Text(formatMoney(balance), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(24.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MonthlyStat("Ingresos", income, true, Modifier.weight(1f)); MonthlyStat("Gastos", expense, false, Modifier.weight(1f))
            }
        }
    }
}

@Composable private fun MonthlyStat(label: String, amount: Double, positive: Boolean, modifier: Modifier) = Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (positive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward, null, modifier = Modifier.size(18.dp), tint = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error); Spacer(Modifier.width(8.dp)); Column { Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(formatMoney(amount), style = MaterialTheme.typography.labelLarge) } } }

@Composable private fun QuickActions(nav: NavController) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { OutlinedButton(onClick = { nav.navigate(Routes.Accounts) }, modifier = Modifier.weight(1f).height(50.dp)) { Text("Cuentas") }; OutlinedButton(onClick = { nav.navigate(Routes.Categories) }, modifier = Modifier.weight(1f).height(50.dp)) { Text("Categorías") } }

@Composable private fun ExpensesCard(slices: List<ExpenseSlice>) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("Gastos del mes", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExpenseDonut(slices, Modifier.size(116.dp))
                Spacer(Modifier.width(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    slices.forEach { slice ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = ChartColors[slice.colorIndex % ChartColors.size],
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(slice.label, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(formatMoney(slice.amount), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ExpenseDonut(slices: List<ExpenseSlice>, modifier: Modifier) { val total = slices.sumOf { it.amount }.toFloat(); Canvas(modifier) { var angle = -90f; slices.forEach { slice -> val sweep = (slice.amount / total * 360f).toFloat(); drawArc(ChartColors[slice.colorIndex % ChartColors.size], angle, sweep, false, Offset.Zero, Size(size.width, size.height), style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Butt)); angle += sweep + 2f } } }

@Composable private fun WeeklyChart(days: List<DailyExpense>) {
    val max = days.maxOf { it.amount }.coerceAtLeast(1.0)
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("Gasto de los últimos 7 días", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth().height(132.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                days.forEach { day ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                            modifier = Modifier.fillMaxWidth().height((day.amount / max * 96).dp)
                        ) {}
                        Spacer(Modifier.height(8.dp))
                        Text(day.day, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable private fun TransactionRow(transaction: Transaction) { val isIncome = transaction.type == TransactionType.income; val tint = when (transaction.type) { TransactionType.income -> MaterialTheme.colorScheme.primary; TransactionType.expense -> MaterialTheme.colorScheme.error; TransactionType.transfer -> MaterialTheme.colorScheme.secondary }; val icon = when (transaction.type) { TransactionType.income -> Icons.Filled.ArrowUpward; TransactionType.expense -> Icons.Filled.ArrowDownward; TransactionType.transfer -> Icons.Filled.SwapHoriz }; Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Surface(shape = RoundedCornerShape(14.dp), color = tint.copy(alpha = .12f), modifier = Modifier.size(46.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint) } }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(transaction.merchant ?: if (transaction.type == TransactionType.transfer) "Transferencia" else "Movimiento", style = MaterialTheme.typography.bodyLarge, maxLines = 1); Text(transaction.type.label(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text((if (isIncome) "+" else if (transaction.type == TransactionType.expense) "−" else "") + formatMoney(transaction.amount), style = MaterialTheme.typography.labelLarge, color = tint) } }

@Composable private fun EmptyMovement() = Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(10.dp)); Text("Aún no tienes movimientos", style = MaterialTheme.typography.titleLarge); Text("Registra el primero con el botón Movimiento.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun ErrorBanner(message: String) = Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.errorContainer) { Text(message, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer) }
private fun TransactionType.label() = when (this) { TransactionType.income -> "Ingreso"; TransactionType.expense -> "Gasto"; TransactionType.transfer -> "Transferencia" }
private fun formatMoney(value: Double): String = NumberFormat.getCurrencyInstance(
    Locale.Builder().setLanguage("es").setRegion("CO").build()
).apply { maximumFractionDigits = 0 }.format(value)
