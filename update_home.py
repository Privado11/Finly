import re

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'r') as f:
    content = f.read()

sig_old = "fun HomeScreen(onNavigateToAccounts: () -> Unit = {}, onTransactionClick: (String) -> Unit = {}, viewModel: HomeViewModel = hiltViewModel()) {"
sig_new = "fun HomeScreen(onNavigateToAccounts: () -> Unit = {}, onNavigateToHistory: () -> Unit = {}, onTransactionClick: (String) -> Unit = {}, viewModel: HomeViewModel = hiltViewModel()) {"
content = content.replace(sig_old, sig_new)

old_title = """            Text(
                text = "Últimos movimientos",
                style = TextStyle(
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    color = ColorBone
                ),
                modifier = Modifier.padding(bottom = 14.dp)
            )"""
new_title = """            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos movimientos",
                    style = TextStyle(
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = ColorBone
                    )
                )
                Text(
                    text = "Ver todos",
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorBrass
                    ),
                    modifier = Modifier.clickable { onNavigateToHistory() }
                )
            }"""
content = content.replace(old_title, new_title)

# Add Inter to imports if not there
if 'import co.privado.finly.ui.theme.Inter' not in content:
    content = content.replace('import co.privado.finly.ui.theme.Fraunces', 'import co.privado.finly.ui.theme.Fraunces\nimport co.privado.finly.ui.theme.Inter')

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'w') as f:
    f.write(content)
