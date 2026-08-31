import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# 1. Change skipPartiallyExpanded = false to true
content = content.replace('skipPartiallyExpanded = false', 'skipPartiallyExpanded = true')

# 2. Remove modifier = Modifier.fillMaxHeight(0.7f) from ModalBottomSheet
content = re.sub(r'modifier = Modifier\.fillMaxHeight\(0\.7f\).*?\n', '', content)

# 3. Change Column(Modifier.fillMaxSize()) to Column(Modifier.fillMaxWidth().fillMaxHeight(0.7f))
content = content.replace('Column(Modifier.fillMaxSize().padding(horizontal = 24.dp))', 'Column(Modifier.fillMaxWidth().fillMaxHeight(0.7f).padding(horizontal = 24.dp))')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
