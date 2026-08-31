import re

with open('app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('@androidx.compose.material3.ExperimentalMaterial3Api\n                TransactionsScreen', 'TransactionsScreen')
content = content.replace('@Composable\nfun MainScreen', '@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun MainScreen')

with open('app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt', 'w') as f:
    f.write(content)
