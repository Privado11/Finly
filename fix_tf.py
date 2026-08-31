with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Add imports for VisualTransformation
if 'import androidx.compose.ui.text.input.VisualTransformation' not in content:
    content = content.replace('import androidx.compose.ui.text.input.KeyboardType', 
                              'import androidx.compose.ui.text.input.KeyboardType\nimport androidx.compose.ui.text.input.VisualTransformation\nimport co.privado.finly.util.CurrencyVisualTransformation')

# Update FinlyTextField signature
old_sig = """fun FinlyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isBig: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
) {"""
new_sig = """fun FinlyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isBig: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    prefix: @Composable (() -> Unit)? = null
) {"""
content = content.replace(old_sig, new_sig)

# Update BasicTextField call
old_btf = """                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                cursorBrush = SolidColor(ColorBrass),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontFamily = if (isBig) Fraunces else Inter,
                                fontSize = if (isBig) 26.sp else 15.sp,
                                color = Color(0xFF4C555F)
                            )
                        )
                    }
                    innerTextField()
                }
            )"""

new_btf = """                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                cursorBrush = SolidColor(ColorBrass),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = visualTransformation,
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (prefix != null) {
                            prefix()
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = TextStyle(
                                        fontFamily = if (isBig) Fraunces else Inter,
                                        fontSize = if (isBig) 26.sp else 15.sp,
                                        color = Color(0xFF4C555F)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )"""
content = content.replace(old_btf, new_btf)

# Update Monto usage
old_monto = """                // Monto
                FinlyTextField(
                    label = "Monto (COP)",
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = "$0",
                    isBig = true,
                    keyboardType = KeyboardType.Decimal
                )"""
new_monto = """                // Monto
                FinlyTextField(
                    label = "Monto (COP)",
                    value = amount,
                    onValueChange = { if (it.length <= 12) amount = it.filter { char -> char.isDigit() } },
                    placeholder = "0",
                    isBig = true,
                    keyboardType = KeyboardType.Number,
                    visualTransformation = CurrencyVisualTransformation(),
                    prefix = {
                        Text(
                            text = "$",
                            style = TextStyle(
                                fontFamily = Fraunces,
                                fontSize = 26.sp,
                                color = ColorBrass
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                )"""
content = content.replace(old_monto, new_monto)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
