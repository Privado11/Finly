import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('androidx.compose.material.icons.Icons.Rounded.Search', 'androidx.compose.material.icons.Icons.Default.Search')
content = content.replace('androidx.compose.material.icons.Icons.Rounded.Add', 'androidx.compose.material.icons.Icons.Default.Add')
content = content.replace('androidx.compose.material.icons.Icons.Rounded.AccountBalanceWallet', 'androidx.compose.material.icons.Icons.Default.AccountBalanceWallet')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
