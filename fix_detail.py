with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'r') as f:
    content = f.read()

if 'import androidx.compose.ui.graphics.Color' not in content:
    content = content.replace('import androidx.compose.ui.Modifier', 'import androidx.compose.ui.Modifier\nimport androidx.compose.ui.graphics.Color')

if 'import co.privado.finly.util.toIcon' not in content:
    content = content.replace('import co.privado.finly.ui.theme.*', 'import co.privado.finly.ui.theme.*\nimport co.privado.finly.util.toIcon')

content = content.replace('co.privado.finly.util.toIcon(state.categoryIcon)', 'state.categoryIcon.toIcon()')

with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'w') as f:
    f.write(content)
