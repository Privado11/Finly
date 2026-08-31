with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'r') as f:
    content = f.read()

sig_card_old = "private fun TransactionsCard(transactions: List<Transaction>, categoryNames: Map<String, String>, onTransactionClick: (String) -> Unit) {"
sig_card_new = "private fun TransactionsCard(transactions: List<Transaction>, categoryNames: Map<String, String>, onTransactionClick: (String) -> Unit, onNavigateToHistory: () -> Unit) {"
content = content.replace(sig_card_old, sig_card_new)

call_old = "TransactionsCard(state.recentTransactions, state.categoryNames, onTransactionClick)"
call_new = "TransactionsCard(state.recentTransactions, state.categoryNames, onTransactionClick, onNavigateToHistory)"
content = content.replace(call_old, call_new)

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'w') as f:
    f.write(content)
