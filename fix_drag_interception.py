import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Make sure draggable is imported? We can just fully qualify.
# But `draggable` is a Modifier extension. We MUST import it!
if 'import androidx.compose.foundation.gestures.draggable' not in content:
    content = content.replace('import androidx.compose.foundation.layout.padding', 'import androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.gestures.draggable')

# Replace the Column start
find_str = """                Column(
                    Modifier.fillMaxWidth().layout { measurable, constraints ->"""

replace_str = """                val dummyDragState = androidx.compose.foundation.gestures.rememberDraggableState { }
                Column(
                    Modifier.fillMaxWidth().draggable(state = dummyDragState, orientation = androidx.compose.foundation.gestures.Orientation.Vertical).layout { measurable, constraints ->"""

content = content.replace(find_str, replace_str)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
