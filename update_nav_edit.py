with open('app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt', 'r') as f:
    content = f.read()

# Update TransactionDetailScreen call
old_detail = 'TransactionDetailScreen(onBack = { nav.popBackStack() })'
new_detail = 'TransactionDetailScreen(onBack = { nav.popBackStack() }, onEdit = { id -> nav.navigate("${Routes.AddTransaction}?transactionId=$id") })'
content = content.replace(old_detail, new_detail)

# Update AddTransaction route
old_add = 'composable(Routes.AddTransaction) { TransactionsScreen(initialType = co.privado.finly.domain.model.TransactionType.expense, onSaved = { nav.popBackStack() }, onBack = { nav.popBackStack() }) }'
new_add = """composable(
                route = "${Routes.AddTransaction}?transactionId={transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType; nullable = true })
            ) { TransactionsScreen(initialType = co.privado.finly.domain.model.TransactionType.expense, onSaved = { nav.popBackStack() }, onBack = { nav.popBackStack() }) }"""
content = content.replace(old_add, new_add)

with open('app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt', 'w') as f:
    f.write(content)
