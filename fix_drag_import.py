import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.foundation.layout.*', 'import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.gestures.draggable')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
