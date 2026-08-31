import re

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'r') as f:
    content = f.read()

# 1. Update TxRow amount color
content = content.replace(
    'val amountColor = if (isInc) ColorMoss else if (isExp) ColorClay else ColorBone',
    'val amountColor = if (isInc) ColorMoss else ColorBone'
)

# 2. Update TransactionsCard loop to group by date
old_loop = """            transactions.take(5).forEachIndexed { index, tx ->
                TxRow(tx, categoryNames, categoryIcons, onTransactionClick)
                if (index < transactions.size - 1 && index < 4) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ColorHair))
                }
            }"""

new_loop = """            val today = java.time.LocalDate.now()
            val yesterday = today.minusDays(1)
            val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", java.util.Locale("es", "ES"))
            
            val limitedTxs = transactions.take(5)
            val grouped = limitedTxs.groupBy { tx ->
                val txDate = runCatching { java.time.Instant.parse(tx.date).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }.getOrNull()
                val formattedDate = txDate?.format(dateFormatter)?.replaceFirstChar { it.uppercase() } ?: ""
                when (txDate) {
                    today -> "Hoy • $formattedDate"
                    yesterday -> "Ayer • $formattedDate"
                    null -> "Desconocido"
                    else -> formattedDate
                }
            }
            
            grouped.forEach { (dateHeader, txsForDate) ->
                Text(
                    text = dateHeader,
                    style = TextStyle(fontSize = 13.sp, color = ColorSlate),
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
                txsForDate.forEachIndexed { index, tx ->
                    TxRow(tx, categoryNames, categoryIcons, onTransactionClick)
                    if (index < txsForDate.size - 1) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ColorHair))
                    }
                }
            }"""
content = content.replace(old_loop, new_loop)

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'w') as f:
    f.write(content)
