import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Add notes state
if 'var notes by rememberSaveable { mutableStateOf("") }' not in content:
    content = content.replace(
        'var merchant by rememberSaveable { mutableStateOf("") }',
        'var merchant by rememberSaveable { mutableStateOf("") }\n    var notes by rememberSaveable { mutableStateOf("") }\n    var showNotes by rememberSaveable { mutableStateOf(false) }'
    )

content = content.replace(
    'merchant = tx.merchant ?: ""',
    'merchant = tx.merchant ?: ""\n            notes = tx.description ?: ""'
)

# Add golden title
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

# Add notes UI and bottom margin
notes_ui = """
                // Comercio
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

# Update save call
content = content.replace(
    'viewModel.save(amount, type, account?.id, destination?.id, category?.id, merchant, onSaved)',
    'viewModel.save(amount, type, account?.id, destination?.id, category?.id, merchant, notes, onSaved)'
)

# Update Bottom Sheets
sheet_account_old = """            ModalBottomSheet(
                onDismissRequest = { picker = null },
                containerColor = ColorSurface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {"""

sheet_account_new = """            ModalBottomSheet(
                onDismissRequest = { picker = null },
                containerColor = ColorSurface,
                modifier = Modifier.fillMaxHeight(0.6f)
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxHeight()) {"""

content = content.replace(sheet_account_old, sheet_account_new)

sheet_category_old = """            ModalBottomSheet(
                onDismissRequest = { picker = null },
                containerColor = ColorSurface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {"""

sheet_category_new = """            ModalBottomSheet(
                onDismissRequest = { picker = null },
                containerColor = ColorSurface,
                modifier = Modifier.fillMaxHeight(0.6f)
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxHeight()) {"""

content = content.replace(sheet_category_old, sheet_category_new)

# Add buttons inside bottom sheets
# For Account
acc_list_end = """                        }
                    }
                }
            }
        }
        "category" -> {"""

acc_list_end_new = """                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(1.dp).background(ColorHair))
                    Text(
                        text = "+ Agregar cuenta",
                        style = TextStyle(color = ColorBone, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                        modifier = Modifier.fillMaxWidth().clickable { }.padding(bottom = 24.dp)
                    )
                }
            }
        }
        "category" -> {"""
content = content.replace(acc_list_end, acc_list_end_new)

# For Category
cat_list_end = """                        }
                    }
                }
            }
        }
    }"""

cat_list_end_new = """                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(1.dp).background(ColorHair))
                    Text(
                        text = "+ Agregar categoría",
                        style = TextStyle(color = ColorBone, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                        modifier = Modifier.fillMaxWidth().clickable { }.padding(bottom = 24.dp)
                    )
                }
            }
        }
    }"""
content = content.replace(cat_list_end, cat_list_end_new)


with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
