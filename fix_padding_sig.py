import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '.padding(horizontal = 20.dp, top = 24.dp, bottom = 24.dp)',
    '.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
