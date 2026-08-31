import re

with open('app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.material.icons.filled.Home', 'import androidx.compose.material.icons.filled.Home\nimport androidx.compose.material.icons.filled.PieChart')
content = content.replace('import androidx.compose.material.icons.outlined.Home', 'import androidx.compose.material.icons.outlined.Home\nimport androidx.compose.material.icons.outlined.PieChart')

old_nav = 'BottomNavItem(Routes.History, "Movim.", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)'
new_nav = 'BottomNavItem(Routes.Stats, "Estadís.", Icons.Filled.PieChart, Icons.Outlined.PieChart)'
content = content.replace(old_nav, new_nav)

content = content.replace('import co.privado.finly.ui.screens.history.HistoryScreen', 'import co.privado.finly.ui.screens.history.HistoryScreen\nimport co.privado.finly.ui.screens.stats.StatsScreen')

# Add the Stats composable
old_comp = 'composable(Routes.Accounts) { AccountsScreen() }'
new_comp = 'composable(Routes.Accounts) { AccountsScreen() }\n            composable(Routes.Stats) { StatsScreen() }'
content = content.replace(old_comp, new_comp)

with open('app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt', 'w') as f:
    f.write(content)
