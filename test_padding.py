import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '.imePadding()\n                    .verticalScroll(rememberScrollState())\n                    .padding(horizontal = 20.dp, vertical = 24.dp)',
    '.imePadding()\n                    .padding(bottom = 40.dp)\n                    .verticalScroll(rememberScrollState())\n                    .padding(horizontal = 20.dp, top = 24.dp, bottom = 24.dp)'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
