import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# 1. Add OptIn and initialType
content = content.replace(
    'fun TransactionsScreen(onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel())',
    '@androidx.compose.material3.ExperimentalMaterial3Api\nfun TransactionsScreen(initialType: co.privado.finly.domain.model.TransactionType = co.privado.finly.domain.model.TransactionType.expense, onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel())'
)
content = content.replace(
    'var type by rememberSaveable { mutableStateOf(TransactionType.expense) }',
    'var type by rememberSaveable { mutableStateOf(initialType) }'
)

# 2. Fix Account -> AccountBalance
content = content.replace('List<Account>', 'List<co.privado.finly.domain.model.AccountBalance>')
content = content.replace('(Account)', '(co.privado.finly.domain.model.AccountBalance)')
content = content.replace('var account by remember { mutableStateOf<Account?>(null) }', 'var account by remember { mutableStateOf<co.privado.finly.domain.model.AccountBalance?>(null) }')
content = content.replace('var destination by remember { mutableStateOf<Account?>(null) }', 'var destination by remember { mutableStateOf<co.privado.finly.domain.model.AccountBalance?>(null) }')

# 3. Fix state access
content = content.replace('val state by viewModel.uiState.collectAsState()', 'val state by viewModel.state.collectAsState()')
content = content.replace('val state by viewModel.state.collectAsState()', 'val state by viewModel.uiState.collectAsState()')

# 4. Remove transfer logic
content = re.sub(r'TransactionType\.transfer to "Transferencia",?\s*', '', content)
content = re.sub(r'if \(type == TransactionType\.transfer\) \{.*?\}', '', content, flags=re.DOTALL)

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

# Add Note
if 'var notes by rememberSaveable' not in content:
    content = content.replace('var merchant by rememberSaveable { mutableStateOf("") }', 'var merchant by rememberSaveable { mutableStateOf("") }\n    var notes by rememberSaveable { mutableStateOf("") }')

content = content.replace('viewModel.save(type, amount, account, destination, category, merchant, date)', 'viewModel.save(type, amount, account, category, merchant, date, notes.takeIf { it.isNotBlank() })')

notes_ui = """
                // Nota
                FinlyTextField(
                    label = "Nota (opcional)",
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Ej. Almuerzo de trabajo"
                )
"""
if 'label = "Nota' not in content:
    content = content.replace('// Comerciante', notes_ui + '\n                // Comerciante')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
