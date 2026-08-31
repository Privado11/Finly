import re

with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val categoryName: String = "",\n    val isDeleting: Boolean = false',
    'val categoryName: String = "",\n    val categoryIcon: String = "",\n    val isDeleting: Boolean = false'
)

content = content.replace(
    'val catName = categories.find { it.id == tx.categoryId }?.name ?: "Sin categoría"',
    'val cat = categories.find { it.id == tx.categoryId }\n            val catName = cat?.name ?: "Sin categoría"\n            val catIcon = cat?.icon ?: ""'
)

content = content.replace(
    'destinationAccountName = destName,\n                categoryName = catName',
    'destinationAccountName = destName,\n                categoryName = catName,\n                categoryIcon = catIcon'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailViewModel.kt', 'w') as f:
    f.write(content)
