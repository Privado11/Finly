import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# 1. We need to add the dummy dispatcher and connection inside AccountPicker and CategoryPicker
account_picker_setup = """    val dummyDispatcher = remember { androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher() }
    val dummyConnection = remember { object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {} }
    androidx.compose.material3.ModalBottomSheet("""

category_picker_setup = """    val dummyDispatcher = remember { androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher() }
    val dummyConnection = remember { object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {} }
    androidx.compose.material3.ModalBottomSheet("""

content = content.replace('    androidx.compose.material3.ModalBottomSheet(', account_picker_setup, 1)
# wait, replacing the first one will replace AccountPicker. The second will replace CategoryPicker.
# It's better to just regex it properly.

content = re.sub(
    r'(var searchQuery by remember \{ mutableStateOf\(""\) \}\n\s*)(androidx\.compose\.material3\.ModalBottomSheet\()',
    r'\1val dummyDispatcher = remember { androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher() }\n    val dummyConnection = remember { object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {} }\n    \2',
    content
)

# 2. Change skipPartiallyExpanded = true to false
content = content.replace('skipPartiallyExpanded = true', 'skipPartiallyExpanded = false')

# 3. Change fillMaxHeight(0.7f) to fillMaxHeight(0.95f)
content = content.replace('Modifier.fillMaxWidth().fillMaxHeight(0.7f).padding', 'Modifier.fillMaxWidth().fillMaxHeight(0.95f).padding')

# 4. Add nestedScroll to LazyColumn
content = content.replace(
    'androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth().weight(1f))',
    'androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth().weight(1f).androidx.compose.ui.input.nestedscroll.nestedScroll(dummyConnection, dummyDispatcher))'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
