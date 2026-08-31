import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# 1. We keep dummyDispatcher to prevent list scrolling from dragging the sheet (if they liked that).
# 2. Change skipPartiallyExpanded = false BACK to true
content = content.replace('skipPartiallyExpanded = false', 'skipPartiallyExpanded = true')

# 3. Change fillMaxHeight(0.95f) BACK to fillMaxHeight(0.7f)
content = content.replace('Modifier.fillMaxWidth().fillMaxHeight(0.95f).padding', 'Modifier.fillMaxWidth().fillMaxHeight(0.7f).padding')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
