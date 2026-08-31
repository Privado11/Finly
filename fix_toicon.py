import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('co.privado.finly.util.toIcon(child.icon)', 'child.icon.co_privado_finly_util_toIcon()')
# Wait, Kotlin doesn't allow calling extensions with full package name like that!
# I need to import it and call `child.icon.toIcon()`
