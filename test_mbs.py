with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

dummy = """
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun TestMBS() {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = {},
        wrongArg = true
    ) {}
}
"""

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content + "\n" + dummy)
