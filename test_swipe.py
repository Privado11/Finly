with open('app/src/main/java/co/privado/finly/MainActivity.kt', 'r') as f:
    content = f.read()

dummy = """
@androidx.compose.material3.ExperimentalMaterial3Api
@androidx.compose.runtime.Composable
fun TestSwipe() {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = {},
        sheetSwipeEnabled = false
    ) {}
}
"""

with open('app/src/main/java/co/privado/finly/MainActivity.kt', 'w') as f:
    f.write(content + "\n" + dummy)
