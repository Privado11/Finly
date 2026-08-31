import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Add import
if 'import androidx.compose.ui.input.nestedscroll.nestedScroll' not in content:
    content = content.replace('import androidx.compose.foundation.lazy.items', 'import androidx.compose.foundation.lazy.items\nimport androidx.compose.ui.input.nestedscroll.nestedScroll')

# Fix usage
content = content.replace('.androidx.compose.ui.input.nestedscroll.nestedScroll', '.nestedScroll')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
