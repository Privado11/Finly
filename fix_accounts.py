with open('app/src/main/java/co/privado/finly/ui/screens/accounts/AccountsScreen.kt', 'r') as f:
    content = f.read()

old_header = """            Text(
                text = "PATRIMONIO TOTAL",
                style = TypographyEyebrow,
                color = ColorBrass,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 4.dp, top = 24.dp)
            )
            val totalBalance = uiState.accounts.filter { it.active }.sumOf { it.balance }
            val formattedTotal = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
                maximumFractionDigits = 0
            }.format(totalBalance)
            Text(
                text = formattedTotal,
                fontFamily = Fraunces,
                fontSize = 32.sp,
                color = ColorBone,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)
            )"""

new_header = """            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)) {
                Text(
                    text = "CUENTAS",
                    style = TypographyEyebrow,
                    color = ColorBrass,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Tus lugares financieros",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Medium,
                        fontSize = 26.sp,
                        color = ColorBone
                    )
                )
            }"""

content = content.replace(old_header, new_header)

with open('app/src/main/java/co/privado/finly/ui/screens/accounts/AccountsScreen.kt', 'w') as f:
    f.write(content)
