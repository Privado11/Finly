import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '@Composable\nfun TransactionsScreen',
    '@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun TransactionsScreen'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
