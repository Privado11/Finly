with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsViewModel.kt', 'r') as f:
    content = f.read()

# Fix Transaction instantiation to include description
old_newTx = "val newTx = Transaction(id = transactionId ?: \"\", sourceAccountId = sourceId, destinationAccountId = null, categoryId = categoryId, type = type, amount = value, merchant = merchant.trim().ifBlank { null }, source = txSource, date = txDate)"
new_newTx = "val newTx = Transaction(id = transactionId ?: \"\", sourceAccountId = sourceId, destinationAccountId = null, categoryId = categoryId, type = type, amount = value, merchant = merchant.trim().ifBlank { null }, description = notes?.trim()?.ifBlank { null }, source = txSource, date = txDate)"
content = content.replace(old_newTx, new_newTx)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsViewModel.kt', 'w') as f:
    f.write(content)
