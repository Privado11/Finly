import re

with open('app/src/main/java/co/privado/finly/ui/navigation/AppNavGraph.kt', 'r') as f:
    content = f.read()

content = content.replace('@androidx.compose.material3.ExperimentalMaterial3Api\n            composable(Routes.Main)', 'composable(Routes.Main)')
content = content.replace('@Composable\nfun AppNavGraph', '@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun AppNavGraph')

with open('app/src/main/java/co/privado/finly/ui/navigation/AppNavGraph.kt', 'w') as f:
    f.write(content)
