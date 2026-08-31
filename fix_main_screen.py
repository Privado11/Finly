import re

with open('app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt', 'r') as f:
    content = f.read()

# Add OptIn to the composable that contains this or just around TransactionsScreen
content = content.replace('TransactionsScreen(initialType', '@androidx.compose.material3.ExperimentalMaterial3Api\n                TransactionsScreen(initialType')
content = content.replace('TransactionsScreen(onSaved', '@androidx.compose.material3.ExperimentalMaterial3Api\n                TransactionsScreen(onSaved')

with open('app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    ts_content = f.read()

ts_content = ts_content.replace(
    'fun TransactionsScreen(onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel())',
    'fun TransactionsScreen(initialType: co.privado.finly.domain.model.TransactionType = co.privado.finly.domain.model.TransactionType.expense, onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel())'
)

# And make sure type initializes from initialType
ts_content = ts_content.replace(
    'var type by rememberSaveable { mutableStateOf(TransactionType.expense) }',
    'var type by rememberSaveable { mutableStateOf(initialType) }'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(ts_content)
