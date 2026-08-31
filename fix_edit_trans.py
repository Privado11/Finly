import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

effect = """
    LaunchedEffect(state.initialTransaction) {
        state.initialTransaction?.let { tx ->
            amount = tx.amount.toLong().toString()
            merchant = tx.merchant ?: ""
            type = tx.type
            account = state.accounts.find { it.id == tx.sourceAccountId }
            category = state.categories.find { it.id == tx.categoryId }
        }
    }
"""

if 'LaunchedEffect(state.initialTransaction)' not in content:
    content = content.replace(
        'var picker by remember { mutableStateOf<String?>(null) }',
        'var picker by remember { mutableStateOf<String?>(null) }\n' + effect
    )

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
