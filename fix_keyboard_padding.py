import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '.fillMaxSize()\n                    .verticalScroll',
    '.fillMaxSize()\n                    .imePadding()\n                    .verticalScroll'
)

# Ensure imePadding is imported, but usually it's in foundation.layout which is probably imported via `import androidx.compose.foundation.layout.*`.
# Let's check imports just in case.

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
