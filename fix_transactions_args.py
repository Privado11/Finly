import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'fun TransactionsScreen(onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel()) {',
    'fun TransactionsScreen(initialType: TransactionType = TransactionType.expense, onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel()) {'
)
content = content.replace(
    'var type by rememberSaveable { mutableStateOf(TransactionType.expense) }',
    'var type by rememberSaveable { mutableStateOf(initialType) }'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
