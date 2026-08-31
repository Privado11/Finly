import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# I will replace Modifier.fillMaxHeight(0.9f) with Modifier.fillMaxHeight(0.55f) on the ModalBottomSheet itself.
content = content.replace('Modifier.fillMaxHeight(0.9f)', 'Modifier.fillMaxHeight(0.55f)')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
