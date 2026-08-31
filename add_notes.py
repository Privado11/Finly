import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Add notes to state
if 'var notes by rememberSaveable' not in content:
    content = content.replace('var merchant by rememberSaveable { mutableStateOf("") }', 'var merchant by rememberSaveable { mutableStateOf("") }\n    var notes by rememberSaveable { mutableStateOf("") }')

# Fix viewModel.save parameters again just in case (the previous script replaced with null)
content = content.replace('viewModel.save(type, amount, account, category, merchant, date, null)', 'viewModel.save(type, amount, account, category, merchant, date, notes.takeIf { it.isNotBlank() })')

# Add Notes UI field after Merchant
notes_ui = """
                // Nota
                FinlyTextField(
                    label = "Nota (opcional)",
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Ej. Almuerzo de trabajo"
                )
"""
if 'label = "Nota' not in content:
    content = content.replace('// Comerciante', notes_ui + '\n                // Comerciante')

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
