package co.privado.finly.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.domain.model.Transaction
import co.privado.finly.domain.model.TransactionType
import co.privado.finly.ui.theme.*
import co.privado.finly.util.toIcon
import androidx.compose.material3.Icon
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.runtime.DisposableEffect
import co.privado.finly.ui.screens.history.TransactionFilter

@Composable
fun HistoryScreen(onTransactionClick: (String) -> Unit = {}, viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.setFilter(TransactionFilter.ALL)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorInk)
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                CircularProgressIndicator(color = ColorBrass) 
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
            ) {
                item {
                    // Topbar
                    Column(modifier = Modifier.padding(bottom = 20.dp)) {
                        Text(
                            text = "HISTORIAL",
                            style = TypographyEyebrow,
                            color = ColorBrass,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Tus movimientos",
                            style = TextStyle(
                                fontFamily = Fraunces,
                                fontWeight = FontWeight.Medium,
                                fontSize = 26.sp,
                                color = ColorBone
                            )
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val options = listOf(
                                TransactionFilter.ALL to "Todos",
                                TransactionFilter.INCOME to "Ingresos",
                                TransactionFilter.EXPENSE to "Gastos"
                            )
                            options.forEach { (filterOption, label) ->
                                val selected = state.filter == filterOption
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (selected) ColorBrass else ColorSurface)
                                        .border(1.dp, if (selected) Color.Transparent else ColorHair, RoundedCornerShape(16.dp))
                                        .clickable { viewModel.setFilter(filterOption) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                            color = if (selected) Color(0xFF1A1305) else ColorBone
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                
                state.error?.let { err ->
                    item {
                        Text(text = err, color = ColorClay, modifier = Modifier.padding(bottom = 16.dp))
                    }
                }

                if (state.groupedTransactions.isEmpty()) {
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
                } else {
                    state.groupedTransactions.forEach { (dateHeader, transactions) ->
                        item {
                            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateHeader,
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            color = ColorSlate
                                        )
                                    )
                                    val dailyTotal = transactions.sumOf { if (it.type == TransactionType.income) it.amount else -it.amount }
                                    val prefix = if (dailyTotal > 0) "+" else if (dailyTotal < 0) "−" else ""
                                    val color = if (dailyTotal > 0) ColorMoss else ColorSlate
                                    Text(
                                        text = "$prefix${formatMoney(kotlin.math.abs(dailyTotal))}",
                                        style = TextStyle(
                                            fontFamily = IbmPlexMono,
                                            fontSize = 13.sp,
                                            color = color
                                        )
                                    )
                                }
                                TransactionsGroupCard(transactions, state.categoryNames, state.categoryIcons, onTransactionClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionsGroupCard(transactions: List<Transaction>, categoryNames: Map<String, String>, categoryIcons: Map<String, String>, onTransactionClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ColorSurface)
            .border(1.dp, ColorHair, RoundedCornerShape(22.dp))
            .padding(top = 8.dp, bottom = 8.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            transactions.forEachIndexed { index, tx ->
                TxRow(tx, categoryNames, categoryIcons, onTransactionClick)
                if (index < transactions.size - 1) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ColorHair))
                }
            }
        }
    }
}

@Composable
private fun TxRow(tx: Transaction, categoryNames: Map<String, String>, categoryIcons: Map<String, String>, onClick: (String) -> Unit) {
    val isInc = tx.type == TransactionType.income
    val isExp = tx.type == TransactionType.expense
    
    val iconColor = if (isInc) ColorMoss else if (isExp) ColorClay else ColorSlate
    val amountColor = if (isInc) ColorMoss else ColorBone
    val prefix = if (isInc) "+" else if (isExp) "−" else ""

    val catName = tx.categoryId?.let { categoryNames[it] } ?: tx.type.label()
    val catIconStr = tx.categoryId?.let { categoryIcons[it] }
    val defaultTitle = tx.categoryId?.let { categoryNames[it] } ?: "Movimiento"
    val displayTitle = tx.merchant?.takeIf { it.isNotBlank() } ?: defaultTitle

    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable { tx.id?.let { onClick(it) } }
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (catIconStr != null && catIconStr.isNotBlank()) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color(0xFF162B28)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = catIconStr.toIcon(),
                    contentDescription = null,
                    tint = ColorMoss,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            val iconBg = if (isInc) ColorMoss.copy(alpha = 0.16f) else if (isExp) ColorClay.copy(alpha = 0.16f) else ColorSlate.copy(alpha = 0.16f)
            val iconText = if (isInc) "↑" else if (isExp) "↓" else "↔"
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconText, color = iconColor, fontSize = 15.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = displayTitle, style = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = ColorBone), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = catName, style = TextStyle(fontSize = 11.5.sp, color = ColorSlate), modifier = Modifier.padding(top = 1.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = "$prefix${formatMoney(tx.amount)}",
            style = TextStyle(fontFamily = IbmPlexMono, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, color = amountColor),
            maxLines = 1
        )
    }
}

private fun TransactionType.label() = when (this) { 
    TransactionType.income -> "Ingreso"
    TransactionType.expense -> "Gasto"
}

private fun formatMoney(value: Double): String = "$" + NumberFormat.getNumberInstance(
    Locale.Builder().setLanguage("es").setRegion("CO").build()
).apply { maximumFractionDigits = 0 }.format(value)
