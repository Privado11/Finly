import re

with open('app/src/main/java/co/privado/finly/ui/navigation/AppNavGraph.kt', 'r') as f:
    content = f.read()

content = content.replace('composable(Routes.Main) { MainScreen() }', '@androidx.compose.material3.ExperimentalMaterial3Api\n            composable(Routes.Main) { MainScreen() }')

with open('app/src/main/java/co/privado/finly/ui/navigation/AppNavGraph.kt', 'w') as f:
    f.write(content)
