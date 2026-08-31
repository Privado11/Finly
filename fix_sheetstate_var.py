import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# For AccountPicker
content = content.replace(
    'var searchQuery by remember { mutableStateOf("") }\n    val dummyDispatcher',
    'var searchQuery by remember { mutableStateOf("") }\n    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)\n    val dummyDispatcher'
)

# And remove it from the ModalBottomSheet invocation
content = content.replace(
    'sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false),',
    'sheetState = sheetState,'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
