with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Introduce canSave boolean before the button Box
new_btn_logic = """                Spacer(modifier = Modifier.height(12.dp))

                val canSave = !state.isSaving && account != null && category != null && amount.isNotBlank() && amount != "0"

                // Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (!canSave) ColorSurfaceHi else ColorBrass)
                        .clickable(enabled = canSave) {"""

content = content.replace("""                Spacer(modifier = Modifier.height(12.dp))

                // Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (state.isSaving || state.accounts.isEmpty()) ColorSurfaceHi else ColorBrass)
                        .clickable(enabled = !state.isSaving && state.accounts.isNotEmpty()) {""", new_btn_logic)

content = content.replace("color = if (state.accounts.isEmpty()) ColorSlate else Color(0xFF1A1305)", "color = if (!canSave) ColorSlate else Color(0xFF1A1305)")

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
