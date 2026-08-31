import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

if 'import androidx.compose.foundation.lazy.items' not in content:
    content = content.replace('import androidx.compose.foundation.lazy.LazyColumn', 'import androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.items')
    # Or just add it at the top
    content = content.replace('package co.privado.finly.ui.screens.transactions', 'package co.privado.finly.ui.screens.transactions\n\nimport androidx.compose.foundation.lazy.items')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
