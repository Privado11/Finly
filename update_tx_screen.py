import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# 1. State
content = content.replace(
    'var merchant by rememberSaveable { mutableStateOf("") }',
    'var merchant by rememberSaveable { mutableStateOf("") }\n    var notes by rememberSaveable { mutableStateOf("") }\n    var showNotes by rememberSaveable { mutableStateOf(false) }'
)
content = content.replace(
    'merchant = tx.merchant ?: ""',
    'merchant = tx.merchant ?: ""\n            notes = tx.description ?: ""'
)

# 2. Golden text
content = content.replace(
    """                        Text(
                            text = if (state.initialTransaction != null) "Editar movimiento" else "Nuevo movimiento",""",
    """                        Text(
                            text = "FINLY",
                            style = TypographyEyebrow,
                            color = ColorBrass,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = if (state.initialTransaction != null) "Editar movimiento" else "Nuevo movimiento","""
)

# 3. Notes UI
notes_ui = """                // Comercio
                FinlyTextField(
                    label = if (type == TransactionType.expense) "Comercio (opcional)" else "Descripción (opcional)",
                    value = merchant,
                    onValueChange = { merchant = it },
                    placeholder = when (type) {
                        TransactionType.expense -> "Ej. Rappi, Éxito..."
                        TransactionType.income -> "Ej. Quincena..."
                    },
                    isBig = false
                )
                
                if (!showNotes && notes.isBlank()) {
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
                }

                Spacer(modifier = Modifier.height(32.dp))"""

content = re.sub(
    r'                // Comercio.*?isBig = false\n                \)',
    notes_ui.strip('\n'),
    content,
    flags=re.DOTALL
)

content = content.replace('Spacer(modifier = Modifier.height(12.dp))', '')

# 4. Save call
content = content.replace(
    'viewModel.save(amount, type, account?.id, destination?.id, category?.id, merchant, onSaved)',
    'viewModel.save(amount, type, account?.id, destination?.id, category?.id, merchant, notes, onSaved)'
)

# 5. AccountPicker Bottom Sheet
acc_sheet_regex = r"""@Composable\nprivate fun AccountPicker\(items: List<co.privado.finly.domain.model.AccountBalance>, select: \(co.privado.finly.domain.model.AccountBalance\) -> Unit, dismiss: \(\) -> Unit\) \{.*?LazyColumn.*?\}
        \}
    \}
\}"""
# Let's just find and replace in AccountPicker
acc_old_bs = """    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss,
        containerColor = co.privado.finly.ui.theme.ColorInk,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {"""

acc_new_bs = """    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss,
        containerColor = co.privado.finly.ui.theme.ColorInk,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false),
        modifier = Modifier.fillMaxHeight(0.6f)
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {"""
content = content.replace(acc_old_bs, acc_new_bs)

acc_list_end = """                }
            }
        }
    }
}"""
acc_list_end_new = """                }
            }
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(1.dp).background(ColorHair))
            Text(
                text = "+ Agregar cuenta",
                style = TextStyle(color = ColorBone, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.fillMaxWidth().clickable { }.padding(bottom = 24.dp)
            )
        }
    }
}"""
# Wait, I need to make sure I only replace it inside AccountPicker!
# I will use a simple split/replace logic to be safe.

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
