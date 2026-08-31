import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '@Composable\nprivate fun AccountPicker',
    '@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nprivate fun AccountPicker'
)
content = content.replace(
    '@Composable\nprivate fun CategoryPicker',
    '@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nprivate fun CategoryPicker'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
