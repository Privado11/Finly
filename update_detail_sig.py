with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'r') as f:
    content = f.read()

sig_old = "fun TransactionDetailScreen(onBack: () -> Unit, viewModel: TransactionDetailViewModel = hiltViewModel()) {"
sig_new = "fun TransactionDetailScreen(onBack: () -> Unit, onEdit: (String) -> Unit = {}, viewModel: TransactionDetailViewModel = hiltViewModel()) {"
content = content.replace(sig_old, sig_new)

edit_old = 'IconButton(onClick = { /* TODO: Edit */ }, modifier = Modifier.size(36.dp)) {'
edit_new = 'IconButton(onClick = { state.transaction?.id?.let { onEdit(it) } }, modifier = Modifier.size(36.dp)) {'
content = content.replace(edit_old, edit_new)

with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'w') as f:
    f.write(content)
