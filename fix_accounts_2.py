with open('app/src/main/java/co/privado/finly/ui/screens/accounts/AccountsScreen.kt', 'r') as f:
    content = f.read()

old_header = """            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)) {
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
                
                val totalBalance = uiState.accounts.filter { it.active }.sumOf { it.balance }
                val formattedTotal = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
                    maximumFractionDigits = 0
                }.format(totalBalance)
                
                Text(
                    text = "Patrimonio total: $formattedTotal",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        color = ColorSlate
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }"""

content = content.replace(old_header, new_header)

with open('app/src/main/java/co/privado/finly/ui/screens/accounts/AccountsScreen.kt', 'w') as f:
    f.write(content)
