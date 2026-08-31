import re
with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Change title
content = content.replace(
    'Text("Nuevo movimiento"',
    'Text(if (state.initialTransaction != null) "Editar movimiento" else "Nuevo movimiento"'
)

# Hide TIPO if editing
old_tipo = """                Text(
                    text = "TIPO",
                    style = TypographyEyebrow,"""
new_tipo = """                if (state.initialTransaction == null) {
                Text(
                    text = "TIPO",
                    style = TypographyEyebrow,"""

content = content.replace(old_tipo, new_tipo)

old_end = """                        }
                    }
                }

                // Monto"""
new_end = """                        }
                    }
                }
                }

                // Monto"""
content = content.replace(old_end, new_end)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
