import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove AlertDialog
content = re.sub(r'\s*if \(showNotes\)\s*\{\s*AlertDialog\([\s\S]*?containerColor = ColorSurface\s*\)\s*\}', '', content)

# 2. Replace the old toggle/text field section with a simple FinlyTextField
old_notes_ui = r"""                if \(notes\.isBlank\(\)\) \{\s*Text\(\s*text = "\+ Agregar nota",[\s\S]*?\} else \{\s*Row\([\s\S]*?\}\s*\}"""

new_notes_ui = """                Spacer(modifier = Modifier.height(16.dp))
                
                // Nota
                FinlyTextField(
                    label = "Nota (opcional)",
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Detalles adicionales...",
                    isBig = false
                )"""

content = re.sub(old_notes_ui, new_notes_ui, content)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
