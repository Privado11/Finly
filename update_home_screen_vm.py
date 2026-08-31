import re

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'r') as f:
    content = f.read()

# Replace local state with ViewModel state
content = content.replace(
    'var isVisible by rememberSaveable { mutableStateOf(true) }',
    'val isVisible by viewModel.isBalancesVisible.collectAsState()'
)

content = content.replace(
    'onClick = { isVisible = !isVisible }',
    'onClick = { viewModel.toggleBalancesVisibility() }'
)

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'w') as f:
    f.write(content)
