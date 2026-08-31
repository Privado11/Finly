with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Fix items() usage inside LazyColumn
content = content.replace('androidx.compose.foundation.lazy.items(items)', 'items(items)')
content = content.replace('androidx.compose.foundation.lazy.items(children)', 'items(children)')
# Fix toIcon() usage
content = content.replace('co.privado.finly.util.toIcon(child.icon)', 'child.icon.toIcon()')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
