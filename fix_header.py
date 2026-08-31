with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

old_header = """                    Column {
                        Text(
                            text = if (state.initialTransaction != null) "Editar movimiento" else "Nuevo movimiento","""

new_header = """                    Column {
                        Text(
                            text = "MOVIMIENTO",
                            style = TypographyEyebrow,
                            color = ColorBrass,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = if (state.initialTransaction != null) "Editar movimiento" else "Nuevo movimiento","""

content = content.replace(old_header, new_header)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
