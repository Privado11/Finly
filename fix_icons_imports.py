import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports_to_add = """
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
"""
content = content.replace('import androidx.compose.material.icons.Icons', imports_to_add.strip() + '\nimport androidx.compose.material.icons.Icons')

# Replace usages
content = content.replace('androidx.compose.material.icons.Icons.Default.Search', 'Icons.Filled.Search')
content = content.replace('androidx.compose.material.icons.Icons.Default.Add', 'Icons.Filled.Add')
content = content.replace('androidx.compose.material.icons.Icons.Default.AccountBalanceWallet', 'Icons.Filled.AccountBalanceWallet')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
