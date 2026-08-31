with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Add LaunchedEffect
launched_effect = """    var picker by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(state.initialTransaction, state.accounts, state.categories) {
        state.initialTransaction?.let { tx ->
            amount = tx.amount.toLong().toString()
            merchant = tx.merchant ?: ""
            notes = tx.description ?: ""
            type = tx.type
            account = state.accounts.find { it.id == tx.sourceAccountId }
            category = state.categories.find { it.id == tx.categoryId }
        }
    }"""
content = content.replace("    var picker by remember { mutableStateOf<String?>(null) }", launched_effect)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
