import re
with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('viewModel.save(amount, type, account?.id, category?.id, merchant, onSaved)', 'viewModel.save(amount, type, account?.id, null, category?.id, merchant, notes.takeIf { it.isNotBlank() }, onSaved)')
with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
