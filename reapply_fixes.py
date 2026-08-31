import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# 1. Imports
if content.startswith('import'):
    lines = content.split('\n')
    pkg_index = next(i for i, line in enumerate(lines) if line.startswith('package'))
    before = lines[:pkg_index]
    rest = lines[pkg_index:]
    content = '\n'.join([rest[0]] + before + rest[1:])

# 2. initialType
content = content.replace('fun TransactionsScreen(onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel())',
    '@androidx.compose.material3.ExperimentalMaterial3Api\nfun TransactionsScreen(initialType: co.privado.finly.domain.model.TransactionType = co.privado.finly.domain.model.TransactionType.expense, onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel())')

content = content.replace('var type by rememberSaveable { mutableStateOf(TransactionType.expense) }',
    'var type by rememberSaveable { mutableStateOf(initialType) }')

# 3. AccountBalance instead of Account in State
content = content.replace('List<Account>', 'List<co.privado.finly.domain.model.AccountBalance>')
content = content.replace('(Account)', '(co.privado.finly.domain.model.AccountBalance)')
content = content.replace('var account by remember { mutableStateOf<Account?>(null) }', 'var account by remember { mutableStateOf<co.privado.finly.domain.model.AccountBalance?>(null) }')
content = content.replace('var destination by remember { mutableStateOf<Account?>(null) }', 'var destination by remember { mutableStateOf<co.privado.finly.domain.model.AccountBalance?>(null) }')

# Fix state accounts accessor if it uses uiState.value.accounts
content = content.replace('val state by viewModel.uiState.collectAsState()', 'val state by viewModel.state.collectAsState()')
content = content.replace('viewModel.save(type, amount, account, destination, category, merchant, date)', 'viewModel.save(type, amount, account, category, merchant, date, null)')

# 4. category visibility
old_cat = """                // Categoría (si no es transferencia)
                if (type != TransactionType.transfer) {
                    FinlyPickerField(
                        label = "Categoría",
                        value = category?.name ?: "Sin categoría",
                        isPlaceholder = category == null,
                        onClick = { picker = "category" }
                    )
                }"""
new_cat = """                // Categoría
                FinlyPickerField(
                    label = "Categoría",
                    value = category?.name ?: "Sin categoría",
                    isPlaceholder = category == null,
                    onClick = { picker = "category" }
                )"""
content = content.replace(old_cat, new_cat)

# Remove destination
old_dest = """                // Destino (si es transferencia)
                if (type == TransactionType.transfer) {
                    FinlyPickerField(
                        label = "Cuenta destino",
                        value = destination?.name ?: "Seleccionar cuenta destino",
                        isPlaceholder = destination == null,
                        onClick = { picker = "destination" }
                    )
                }"""
content = content.replace(old_dest, "")

# Remove transfer type option
content = re.sub(r'TransactionType\.transfer to "Transferencia",?\s*', '', content)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
