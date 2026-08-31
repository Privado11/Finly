import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Fix state reference
content = content.replace('val state by viewModel.state.collectAsState()', 'val state by viewModel.uiState.collectAsState()')

# Add necessary imports
imports = """
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import co.privado.finly.domain.model.Category
"""
content = content.replace("import androidx.compose.ui.Alignment", imports + "\nimport androidx.compose.ui.Alignment")

# Remove `initialType` and revert back because `TransactionsScreen` needs to match `MainScreen.kt` usage.
# Wait, `TransactionsScreen` was already fixed to have `initialType` in my previous fixes.
# Did the `git checkout` revert it? Yes.
content = content.replace('fun TransactionsScreen(onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel())',
    '@androidx.compose.material3.ExperimentalMaterial3Api\nfun TransactionsScreen(initialType: co.privado.finly.domain.model.TransactionType = co.privado.finly.domain.model.TransactionType.expense, onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel())')

content = content.replace('var type by rememberSaveable { mutableStateOf(TransactionType.expense) }',
    'var type by rememberSaveable { mutableStateOf(initialType) }')

# AccountPicker usage has `title` as first parameter
# Replace the old AccountPicker signature
old_acc_picker = """@Composable
private fun AccountPicker(items: List<co.privado.finly.domain.model.AccountBalance>, select: (co.privado.finly.domain.model.AccountBalance) -> Unit, dismiss: () -> Unit) {"""
new_acc_picker = """@Composable
private fun AccountPicker(title: String, items: List<co.privado.finly.domain.model.AccountBalance>, select: (co.privado.finly.domain.model.AccountBalance) -> Unit, dismiss: () -> Unit) {"""
content = content.replace(old_acc_picker, new_acc_picker)

# Fix missing `toIcon` reference in CategoryPicker
# Instead of `cat.icon.toIcon()`, use `co.privado.finly.util.toIcon(cat.icon)`
content = content.replace('cat.icon.toIcon()', 'co.privado.finly.util.toIcon(cat.icon)')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
