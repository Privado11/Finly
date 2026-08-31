with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

if 'import co.privado.finly.util.CurrencyVisualTransformation' not in content:
    content = content.replace('import co.privado.finly.ui.theme.*', 'import co.privado.finly.ui.theme.*\nimport co.privado.finly.util.CurrencyVisualTransformation\nimport androidx.compose.ui.text.input.VisualTransformation')

old_monto = """                FinlyTextField(
                    label = "Monto (COP)",
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = "$0",
                    isBig = true,
                    keyboardType = KeyboardType.Decimal
                )"""
new_monto = """                FinlyTextField(
                    label = "Monto (COP)",
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = "0",
                    isBig = true,
                    keyboardType = KeyboardType.Decimal,
                    visualTransformation = CurrencyVisualTransformation()
                )"""
content = content.replace(old_monto, new_monto)

old_tf_sig = """fun FinlyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isBig: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
)"""
new_tf_sig = """fun FinlyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isBig: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
)"""
content = content.replace(old_tf_sig, new_tf_sig)

old_tf_body = """                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
                }"""
new_tf_body = """                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                cursorBrush = SolidColor(ColorBrass),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = visualTransformation,
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isBig) {
                            Text(
                                text = "$ ",
                                style = TextStyle(
                                    fontFamily = Fraunces,
                                    fontSize = 26.sp,
                                    color = ColorBrass,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Box(Modifier.weight(1f)) {
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
                }"""
content = content.replace(old_tf_body, new_tf_body)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
