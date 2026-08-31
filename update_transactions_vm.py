import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsViewModel.kt', 'r') as f:
    content = f.read()

# Update save method signature
content = content.replace(
    'fun save(amount: String, type: TransactionType, sourceId: String?, destinationId: String?, categoryId: String?, merchant: String, onSuccess: () -> Unit) {',
    'fun save(amount: String, type: TransactionType, sourceId: String?, destinationId: String?, categoryId: String?, merchant: String, notes: String?, onSuccess: () -> Unit) {'
)

# Update insert
content = content.replace(
    'merchant = merchant.takeIf { it.isNotBlank() },',
    'merchant = merchant.takeIf { it.isNotBlank() },\n                    description = notes?.takeIf { it.isNotBlank() },'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsViewModel.kt', 'w') as f:
    f.write(content)
