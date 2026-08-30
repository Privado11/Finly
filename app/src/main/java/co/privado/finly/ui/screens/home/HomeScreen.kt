package co.privado.finly.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.domain.model.Transaction
import co.privado.finly.domain.model.TransactionType
import co.privado.finly.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(onNavigateToAccounts: () -> Unit = {}, onTransactionClick: (String) -> Unit = {}, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorInk)
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ColorBrass) }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
            ) {
                item {
                    // Topbar
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = "FINLY",
                            style = TypographyEyebrow,
                            color = ColorBrass,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Tu resumen",
                            style = TextStyle(
                                fontFamily = Fraunces,
                                fontWeight = FontWeight.Medium,
                                fontSize = 26.sp,
                                color = ColorBone
                            )
                        )
                    }
                }
                
                state.error?.let { err ->
                    item {
                        Text(text = err, color = ColorClay, modifier = Modifier.padding(bottom = 16.dp))
                    }
                }

                item {
                    BalanceCard(state.balance, state.monthlyIncome, state.monthlyExpense)
                }

                if (state.expenseSlices.isNotEmpty()) {
                    item {
                        ExpensesCard(state.expenseSlices)
                    }
                }

                if (state.recentTransactions.isNotEmpty()) {
                    item {
                        TransactionsCard(state.recentTransactions, state.categoryNames, onTransactionClick)
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aún no tienes movimientos", color = ColorSlate, style = Typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: Double, income: Double, expense: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(ColorSurface)
            .border(1.dp, ColorHair, RoundedCornerShape(24.dp))
            .drawBehind {
                val brush = Brush.verticalGradient(
                    0.0f to ColorBrass,
                    0.9f to Color.Transparent
                )
                drawRect(
                    brush = brush,
                    topLeft = Offset(0f, 0f),
                    size = Size(3.dp.toPx(), size.height)
                )
            }
            .padding(top = 22.dp, bottom = 20.dp, start = 26.dp, end = 22.dp)
    ) {
        Column {
            Text("BALANCE TOTAL", style = TypographyEyebrow, color = ColorSlate, modifier = Modifier.padding(bottom = 6.dp))
            
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 18.dp)) {
                Text(
                    text = "$",
                    style = TextStyle(
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        color = ColorBrass
                    )
                )
                Text(
                    text = formatMoneyNoSymbol(balance),
                    style = TextStyle(
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Medium,
                        fontSize = 38.sp,
                        color = ColorBone
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SplitBox(label = "Ingresos", amount = income, dotColor = ColorMoss, modifier = Modifier.weight(1f))
                SplitBox(label = "Gastos", amount = expense, dotColor = ColorClay, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SplitBox(label: String, amount: Double, dotColor: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ColorSurfaceHi)
            .padding(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(dotColor))
                Spacer(Modifier.width(6.dp))
                Text(label, style = TextStyle(fontSize = 11.sp, color = ColorSlate, fontFamily = Inter))
            }
            Text(
                text = formatMoney(amount),
                style = TextStyle(
                    fontFamily = IbmPlexMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = ColorBone
                )
            )
        }
    }
}

@Composable
private fun ExpensesCard(slices: List<ExpenseSlice>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(ColorSurface)
            .border(1.dp, ColorHair, RoundedCornerShape(22.dp))
            .padding(top = 18.dp, bottom = 18.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            Text(
                text = "Gastos del mes",
                style = TextStyle(
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    color = ColorBone
                ),
                modifier = Modifier.padding(bottom = 14.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .padding(end = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        val total = slices.sumOf { it.amount }.toFloat()
                        var currentAngle = -90f
                        val colors = listOf(ColorBrass, ColorClay, ColorMoss, Color(0xFFD69684))
                        
                        if (total > 0) {
                            slices.forEachIndexed { index, slice ->
                                val sweepAngle = (slice.amount.toFloat() / total) * 360f
                                // Leave a tiny 2 degree gap between slices if there's more than one slice
                                val gap = if (slices.size > 1) 2f else 0f
                                
                                drawArc(
                                    color = colors[index % colors.size],
                                    startAngle = currentAngle,
                                    sweepAngle = sweepAngle - gap,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                                currentAngle += sweepAngle
                            }
                        } else {
                            // Empty state (no expenses)
                            drawArc(
                                color = ColorHair,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }
                    val currentMonth = java.time.LocalDate.now().month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("es", "ES")).take(3).replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                    val currentYear = java.time.LocalDate.now().year
                    val dateText = "$currentMonth\n$currentYear"
                    Text(
                        text = dateText,
                        style = TextStyle(
                            fontFamily = IbmPlexMono,
                            fontSize = 11.sp,
                            color = ColorSlate
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    val colors = listOf(ColorBrass, ColorClay, ColorMoss, Color(0xFFD69684))
                    slices.take(4).forEachIndexed { index, slice ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(colors[index % colors.size]))
                            Spacer(Modifier.width(8.dp))
                            Text(text = slice.label, style = TextStyle(fontSize = 12.5.sp, color = ColorBone), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            Text(text = formatMoney(slice.amount), style = TextStyle(fontFamily = IbmPlexMono, fontSize = 12.sp, color = ColorSlate))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionsCard(transactions: List<Transaction>, categoryNames: Map<String, String>, onTransactionClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(ColorSurface)
            .border(1.dp, ColorHair, RoundedCornerShape(22.dp))
            .padding(top = 18.dp, bottom = 18.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            Text(
                text = "Últimos movimientos",
                style = TextStyle(
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    color = ColorBone
                ),
                modifier = Modifier.padding(bottom = 14.dp)
            )
            
            transactions.take(5).forEachIndexed { index, tx ->
                TxRow(tx, categoryNames, onTransactionClick)
                if (index < transactions.size - 1 && index < 4) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ColorHair))
                }
            }
        }
    }
}

@Composable
private fun TxRow(tx: Transaction, categoryNames: Map<String, String>, onClick: (String) -> Unit) {
    val isInc = tx.type == TransactionType.income
    val isExp = tx.type == TransactionType.expense
    
    val iconColor = if (isInc) ColorMoss else if (isExp) ColorClay else ColorSlate
    val iconBg = if (isInc) ColorMoss.copy(alpha = 0.16f) else if (isExp) ColorClay.copy(alpha = 0.16f) else ColorSlate.copy(alpha = 0.16f)
    val iconText = if (isInc) "↑" else if (isExp) "↓" else "↔"
    val amountColor = if (isInc) ColorMoss else if (isExp) ColorClay else ColorBone
    val prefix = if (isInc) "+" else if (isExp) "−" else ""

    val defaultTitle = tx.categoryId?.let { categoryNames[it] } ?: if (tx.type == TransactionType.transfer) "Transferencia" else "Movimiento"
    val displayTitle = tx.merchant?.takeIf { it.isNotBlank() } ?: defaultTitle

    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable { tx.id?.let { onClick(it) } }
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconText, color = iconColor, fontSize = 15.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = displayTitle, style = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = ColorBone), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = tx.type.label(), style = TextStyle(fontSize = 11.5.sp, color = ColorSlate), modifier = Modifier.padding(top = 1.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = "$prefix${formatMoney(tx.amount)}",
            style = TextStyle(fontFamily = IbmPlexMono, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, color = amountColor),
            maxLines = 1
        )
    }
}

private fun TransactionType.label() = when (this) { TransactionType.income -> "Ingreso"; TransactionType.expense -> "Gasto"; TransactionType.transfer -> "Transferencia" }

private fun formatMoneyNoSymbol(value: Double): String = NumberFormat.getNumberInstance(
    Locale.Builder().setLanguage("es").setRegion("CO").build()
).apply { maximumFractionDigits = 0 }.format(value)

private fun formatMoney(value: Double): String = "$" + formatMoneyNoSymbol(value)
