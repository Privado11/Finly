import re

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'r') as f:
    content = f.read()

# Add imports
if 'import androidx.compose.material.icons.Icons' not in content:
    content = content.replace('import androidx.compose.material3.*', 'import androidx.compose.material3.*\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.rounded.*')

if 'import androidx.compose.runtime.saveable.rememberSaveable' not in content:
    content = content.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\nimport androidx.compose.runtime.saveable.rememberSaveable')

# 1. Add isVisible state
if 'var isVisible by rememberSaveable { mutableStateOf(true) }' not in content:
    content = content.replace(
        'val state by viewModel.uiState.collectAsState()',
        'val state by viewModel.uiState.collectAsState()\n    var isVisible by rememberSaveable { mutableStateOf(true) }'
    )

# 2. Update Top Bar
old_top_bar = """                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(ColorSurface)
                                .border(1.dp, ColorHair, androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👋", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
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
                }"""

new_top_bar = """                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(ColorSurface)
                                    .border(1.dp, ColorHair, androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👋", fontSize = 18.sp)
                            }
                            Spacer(Modifier.width(12.dp))
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
                        IconButton(onClick = { isVisible = !isVisible }) {
                            Icon(
                                imageVector = if (isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                contentDescription = "Mostrar balances",
                                tint = ColorSlate
                            )
                        }
                    }
                }"""

content = content.replace(old_top_bar, new_top_bar)

# 3. Update component calls
content = content.replace(
    'BalanceCard(state.balance, state.monthlyIncome, state.monthlyExpense)',
    'BalanceCard(state.balance, state.monthlyIncome, state.monthlyExpense, isVisible)'
)

content = content.replace(
    'ExpensesCard(state.expenseSlices)',
    'ExpensesCard(state.expenseSlices, isVisible)'
)

content = content.replace(
    'TransactionsCard(state.recentTransactions, state.categoryNames, state.categoryIcons, onTransactionClick, onNavigateToHistory)',
    'TransactionsCard(state.recentTransactions, state.categoryNames, state.categoryIcons, isVisible, onTransactionClick, onNavigateToHistory)'
)

# 4. Update signatures and internal texts
content = content.replace(
    'private fun BalanceCard(balance: Double, income: Double, expense: Double) {',
    'private fun BalanceCard(balance: Double, income: Double, expense: Double, isVisible: Boolean) {'
)
content = content.replace(
    'text = formatMoneyNoSymbol(balance),',
    'text = if (isVisible) formatMoneyNoSymbol(balance) else "-.---",'
)
content = content.replace(
    'SplitBox(label = "Ingresos", amount = income, dotColor = ColorMoss, modifier = Modifier.weight(1f))',
    'SplitBox(label = "Ingresos", amount = income, dotColor = ColorMoss, isVisible = isVisible, modifier = Modifier.weight(1f))'
)
content = content.replace(
    'SplitBox(label = "Gastos", amount = expense, dotColor = ColorClay, modifier = Modifier.weight(1f))',
    'SplitBox(label = "Gastos", amount = expense, dotColor = ColorClay, isVisible = isVisible, modifier = Modifier.weight(1f))'
)

content = content.replace(
    'private fun SplitBox(label: String, amount: Double, dotColor: Color, modifier: Modifier) {',
    'private fun SplitBox(label: String, amount: Double, dotColor: Color, isVisible: Boolean, modifier: Modifier) {'
)
content = content.replace(
    'text = formatMoney(amount),',
    'text = if (isVisible) formatMoney(amount) else "$ -.---",'
)

content = content.replace(
    'private fun ExpensesCard(slices: List<ExpenseSlice>) {',
    'private fun ExpensesCard(slices: List<ExpenseSlice>, isVisible: Boolean) {'
)
content = content.replace(
    'text = formatMoney(slice.amount),',
    'text = if (isVisible) formatMoney(slice.amount) else "$ -.---",'
)

content = content.replace(
    'private fun TransactionsCard(transactions: List<Transaction>, categoryNames: Map<String, String>, categoryIcons: Map<String, String>, onTransactionClick: (String) -> Unit, onNavigateToHistory: () -> Unit) {',
    'private fun TransactionsCard(transactions: List<Transaction>, categoryNames: Map<String, String>, categoryIcons: Map<String, String>, isVisible: Boolean, onTransactionClick: (String) -> Unit, onNavigateToHistory: () -> Unit) {'
)
content = content.replace(
    'TxRow(tx, categoryNames, categoryIcons, onTransactionClick)',
    'TxRow(tx, categoryNames, categoryIcons, isVisible, onTransactionClick)'
)

content = content.replace(
    'private fun TxRow(tx: Transaction, categoryNames: Map<String, String>, categoryIcons: Map<String, String>, onClick: (String) -> Unit) {',
    'private fun TxRow(tx: Transaction, categoryNames: Map<String, String>, categoryIcons: Map<String, String>, isVisible: Boolean, onClick: (String) -> Unit) {'
)
content = content.replace(
    'text = "$prefix${formatMoney(tx.amount)}",',
    'text = if (isVisible) "$prefix${formatMoney(tx.amount)}" else "$prefix -.---",'
)

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'w') as f:
    f.write(content)
