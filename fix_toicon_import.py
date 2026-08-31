import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Add import
if 'import co.privado.finly.util.toIcon' not in content:
    content = content.replace('import co.privado.finly.ui.theme.*', 'import co.privado.finly.ui.theme.*\nimport co.privado.finly.util.toIcon')

content = content.replace('co.privado.finly.util.toIcon(child.icon)', 'child.icon.toIcon()')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
