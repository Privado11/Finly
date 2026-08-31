import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# 1. Change skipPartiallyExpanded = true to false
content = content.replace('skipPartiallyExpanded = true', 'skipPartiallyExpanded = false')

# 2. Add the layout modifier to the main Column of both pickers.
# Currently they look like:
# Column(Modifier.fillMaxWidth().fillMaxHeight(0.7f).padding(horizontal = 24.dp)) {

layout_modifier = """Modifier
            .fillMaxWidth()
            .androidx_compose_ui_layout_layout { measurable, constraints ->
                val offset = try {
                    val o = sheetState.requireOffset()
                    if (o.isNaN()) constraints.maxHeight / 2f else o
                } catch(e: Exception) {
                    constraints.maxHeight / 2f
                }
                val visibleHeight = (constraints.maxHeight - offset).toInt().coerceAtLeast(0)
                val placeable = measurable.measure(
                    constraints.copy(minHeight = visibleHeight, maxHeight = visibleHeight)
                )
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.place(0, 0)
                }
            }
            .padding(horizontal = 24.dp)"""

# I need to use the correct import for layout. It's Modifier.layout. 
# But I can just import androidx.compose.ui.layout.layout
import_layout = "import androidx.compose.ui.layout.layout\n"
if 'import androidx.compose.ui.layout.layout' not in content:
    content = content.replace('import androidx.compose.ui.Modifier', 'import androidx.compose.ui.Modifier\nimport androidx.compose.ui.layout.layout')

layout_modifier_clean = """Modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                val offset = try {
                    val o = sheetState.requireOffset()
                    if (o.isNaN()) constraints.maxHeight / 2f else o
                } catch(e: Exception) {
                    constraints.maxHeight / 2f
                }
                val visibleHeight = (constraints.maxHeight - offset).toInt().coerceAtLeast(0)
                val placeable = measurable.measure(
                    constraints.copy(minHeight = visibleHeight, maxHeight = visibleHeight)
                )
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.place(0, 0)
                }
            }
            .padding(horizontal = 24.dp)"""

content = content.replace('Modifier.fillMaxWidth().fillMaxHeight(0.7f).padding(horizontal = 24.dp)', layout_modifier_clean)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
