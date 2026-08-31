import re

with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryScreen.kt', 'r') as f:
    content = f.read()

sig_row_old = "private fun TxRow(tx: Transaction, categoryNames: Map<String, String>, onClick: (String) -> Unit) {"
sig_row_new = "private fun TxRow(tx: Transaction, categoryNames: Map<String, String>, categoryIcons: Map<String, String>, onClick: (String) -> Unit) {"
content = content.replace(sig_row_old, sig_row_new)

tx_call_old = "TxRow(tx, state.categoryNames, onTransactionClick)"
tx_call_new = "TxRow(tx, state.categoryNames, state.categoryIcons, onTransactionClick)"
content = content.replace(tx_call_old, tx_call_new)

body_old = """    val iconColor = if (isInc) ColorMoss else if (isExp) ColorClay else ColorSlate
    val iconBg = if (isInc) ColorMoss.copy(alpha = 0.16f) else if (isExp) ColorClay.copy(alpha = 0.16f) else ColorSlate.copy(alpha = 0.16f)
    val iconText = if (isInc) "↑" else if (isExp) "↓" else "↔"
    val amountColor = if (isInc) ColorMoss else if (isExp) ColorClay else ColorBone
    val prefix = if (isInc) "+" else if (isExp) "−" else ""

    val defaultTitle = tx.categoryId?.let { categoryNames[it] } ?: "Movimiento"
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
        }"""

body_new = """    val iconColor = if (isInc) ColorMoss else if (isExp) ColorClay else ColorSlate
    val amountColor = if (isInc) ColorMoss else if (isExp) ColorClay else ColorBone
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
                    imageVector = co.privado.finly.util.toIcon(catIconStr),
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
        }"""
content = content.replace(body_old, body_new)

if 'import co.privado.finly.util.toIcon' not in content:
    content = content.replace('import co.privado.finly.ui.theme.*', 'import co.privado.finly.ui.theme.*\nimport co.privado.finly.util.toIcon\nimport androidx.compose.material3.Icon')

with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryScreen.kt', 'w') as f:
    f.write(content)
