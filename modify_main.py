import sys

with open("app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt", "r") as f:
    content = f.read()

# Replace the AddTransaction route
old_route = "composable(Routes.AddTransaction) { TransactionsScreen(onSaved = { nav.popBackStack() }, onBack = { nav.popBackStack() }) }"
new_route = """            composable(
                route = "${Routes.AddTransaction}/{type}",
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val typeStr = backStackEntry.arguments?.getString("type")
                val initialType = if (typeStr == "income") co.privado.finly.domain.model.TransactionType.income else co.privado.finly.domain.model.TransactionType.expense
                TransactionsScreen(initialType = initialType, onSaved = { nav.popBackStack() }, onBack = { nav.popBackStack() })
            }
            composable(Routes.AddTransaction) { TransactionsScreen(initialType = co.privado.finly.domain.model.TransactionType.expense, onSaved = { nav.popBackStack() }, onBack = { nav.popBackStack() }) }"""
content = content.replace(old_route, new_route)

# Append Bottom Sheet before the last brace
bottom_sheet = """
    if (showTransactionTypeSheet) {
        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showTransactionTypeSheet = false },
            containerColor = co.privado.finly.ui.theme.ColorSurface,
            sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¿Qué vas a registrar?", fontFamily = co.privado.finly.ui.theme.Fraunces, fontSize = 24.sp, color = co.privado.finly.ui.theme.ColorBone, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(32.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        onClick = { showTransactionTypeSheet = false; nav.navigate("${Routes.AddTransaction}/expense") },
                        modifier = Modifier.weight(1f).aspectRatio(1.2f),
                        shape = RoundedCornerShape(20.dp),
                        color = co.privado.finly.ui.theme.ColorClay.copy(alpha = 0.15f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = co.privado.finly.ui.theme.ColorClay, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Gasto", fontFamily = co.privado.finly.ui.theme.Inter, fontWeight = FontWeight.SemiBold, color = co.privado.finly.ui.theme.ColorClay)
                        }
                    }
                    Surface(
                        onClick = { showTransactionTypeSheet = false; nav.navigate("${Routes.AddTransaction}/income") },
                        modifier = Modifier.weight(1f).aspectRatio(1.2f),
                        shape = RoundedCornerShape(20.dp),
                        color = co.privado.finly.ui.theme.ColorMoss.copy(alpha = 0.15f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = co.privado.finly.ui.theme.ColorMoss, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Ingreso", fontFamily = co.privado.finly.ui.theme.Inter, fontWeight = FontWeight.SemiBold, color = co.privado.finly.ui.theme.ColorMoss)
                        }
                    }
                }
            }
        }
    }
}"""
content = content.rsplit('}', 1)[0] + bottom_sheet

with open("app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt", "w") as f:
    f.write(content)
