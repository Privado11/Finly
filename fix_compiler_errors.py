import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove TransactionType.transfer
content = content.replace("TransactionType.transfer to \"Transferencia\"", "")
content = content.replace("TransactionType.transfer -> \"Nueva transferencia\"", "")
content = content.replace("TransactionType.transfer ->", "else ->") # fallback
content = content.replace("type == TransactionType.transfer", "false")

# 2. Fix Account variables (destination is no longer needed but was there)
content = content.replace("var destination by rememberSaveable { mutableStateOf<Account?>(null) }", "var destination by rememberSaveable { mutableStateOf<co.privado.finly.domain.model.AccountBalance?>(null) }")
content = content.replace("var account by rememberSaveable { mutableStateOf<Account?>(null) }", "var account by rememberSaveable { mutableStateOf<co.privado.finly.domain.model.AccountBalance?>(null) }")

# 3. Fix the lazy column items bug in my rewrite_pickers script!
# In rewrite_pickers, I did:
# items(filtered) { acc ->
# But LazyColumn needs `items(filtered) { acc ->` from `import androidx.compose.foundation.lazy.items`.
# Let's ensure `import androidx.compose.foundation.lazy.items` is present.
if 'import androidx.compose.foundation.lazy.items' not in content:
    content = content.replace('import androidx.compose.foundation.lazy.LazyColumn', 'import androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.items')

# Wait, the compiler error says:
# e: file:///home/walter/AndroidStudioProjects/Finly/app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt:397:31 Argument type mismatch: actual type is 'List<AccountBalance>', but 'Int' was expected.
# That means `items(filtered)` is resolving to `items(count: Int)` because `items(List)` is NOT imported!
# I need to add the import.

# 4. formatMoney is already defined in the rewrite script at the bottom.

# 5. Fix type inference in tabs:
content = content.replace(
    'val tabs = listOf(\n        TransactionType.income to "Ingreso",\n        TransactionType.expense to "Gasto",\n        \n    )',
    'val tabs = listOf<Pair<TransactionType, String>>(\n        TransactionType.income to "Ingreso",\n        TransactionType.expense to "Gasto"\n    )'
)

# 6. Fix `onSuccess = { ... }` in viewmodel.save(...)
# Wait, the error is:
# e: file:///home/walter/AndroidStudioProjects/Finly/app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt:214:112 Argument type mismatch: actual type is 'Function0<Unit>', but 'String?' was expected.
# Let's check where save is called.
content = re.sub(
    r'viewModel\.save\(type, amountDouble, account\?\.id, destination\?\.id, category\?\.id, onSuccess = \{\n',
    r'viewModel.save(type, amountDouble, account?.id, null, category?.id, "", onSuccess = {\n', # added note=""
    content
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
