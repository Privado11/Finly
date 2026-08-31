import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove destination variable
content = re.sub(r'\s*var destination by remember \{ mutableStateOf<AccountBalance\?>\(null\) \}', '', content)

# 2. Fix the account/category fields
old_fields = """                // Cuenta
                FinlyPickerField(
                    label = "Cuenta",
                    value = account?.name ?: "Seleccionar cuenta",
                    isPlaceholder = account == null,
                    onClick = { picker = "account" }
                )

                // Destino (si es transferencia)
                if (type == TransactionType.expense) {
                    FinlyPickerField(
                        label = "Cuenta destino",
                        value = destination?.name ?: "Seleccionar cuenta destino",
                        isPlaceholder = destination == null,
                        onClick = { picker = "destination" }
                    )
                }

                // Categoría (si no es transferencia)
                if (type != TransactionType.expense) {
                    FinlyPickerField(
                        label = "Categoría",
                        value = category?.name ?: "Sin categoría",
                        isPlaceholder = category == null,
                        onClick = { picker = "category" }
                    )
                }"""

new_fields = """                // Cuenta
                FinlyPickerField(
                    label = "Cuenta",
                    value = account?.name ?: "Seleccionar cuenta",
                    isPlaceholder = account == null,
                    onClick = { picker = "account" }
                )

                // Categoría
                FinlyPickerField(
                    label = "Categoría",
                    value = category?.name ?: "Sin categoría",
                    isPlaceholder = category == null,
                    onClick = { picker = "category" }
                )"""

content = content.replace(old_fields, new_fields)

# 3. Fix clearing destination in segmented control
content = content.replace(
    'account = null\n                                        destination = null',
    'account = null'
)

# 4. Remove destination from picker when
content = re.sub(r'\s*"destination" -> AccountPicker.*?,\s*\{ picker = null \}\)', '', content)

# 5. Remove destination from save call
content = content.replace(
    'viewModel.save(amount, type, account?.id, destination?.id, category?.id, merchant, notes, onSaved)',
    'viewModel.save(amount, type, account?.id, null, category?.id, merchant, notes, onSaved)'
)

# 6. Notes Modal logic
notes_ui_old = """                if (!showNotes && notes.isBlank()) {
                    Text(
                        text = "+ Agregar nota",
                        style = TextStyle(color = ColorBrass, fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 4.dp)
                            .clickable { showNotes = true }
                    )
                }
                
                if (showNotes || notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    FinlyTextField(
                        label = "Nota (opcional)",
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "Detalles adicionales...",
                        isBig = false
                    )
                }"""

notes_ui_new = """                if (notes.isBlank()) {
                    Text(
                        text = "+ Agregar nota",
                        style = TextStyle(color = ColorBrass, fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 4.dp)
                            .clickable { showNotes = true }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp).clickable { showNotes = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nota adjunta", style = TypographyEyebrow, color = ColorBrass)
                            Text(notes, color = ColorBone, fontSize = 14.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                        Text("Editar", color = ColorSlate, fontSize = 13.sp)
                    }
                }"""

content = content.replace(notes_ui_old, notes_ui_new)

# Add the AlertDialog for notes at the end of the composable (before the last bracket)
modal_logic = """
    if (showNotes) {
        AlertDialog(
            onDismissRequest = { showNotes = false },
            title = { Text("Nota", color = ColorBone) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Detalles adicionales...", color = ColorSlate) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorBone,
                        unfocusedTextColor = ColorBone,
                        focusedBorderColor = ColorBrass,
                        unfocusedBorderColor = ColorHair,
                        cursorColor = ColorBrass
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { showNotes = false }) {
                    Text("Listo", color = ColorBrass)
                }
            },
            dismissButton = {
                TextButton(onClick = { notes = ""; showNotes = false }) {
                    Text("Borrar", color = ColorClay)
                }
            },
            containerColor = ColorSurface
        )
    }
"""
content = re.sub(r'(\s*when \(picker\))', modal_logic + r'\1', content)


with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
