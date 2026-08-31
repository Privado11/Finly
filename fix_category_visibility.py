import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Remove the incorrect if block completely.
old_block = """
                // Categoría (si no es transferencia)
                if (type != TransactionType.expense) {
                    FinlyPickerField(
                        label = "Categoría",
                        value = category?.name ?: "Sin categoría",
                        isPlaceholder = category == null,
                        onClick = { picker = "category" }
                    )
                }
"""

new_block = """
                // Categoría
                FinlyPickerField(
                    label = "Categoría",
                    value = category?.name ?: "Sin categoría",
                    isPlaceholder = category == null,
                    onClick = { picker = "category" }
                )
"""

if old_block.strip() in content:
    content = content.replace(old_block.strip(), new_block.strip())
else:
    # Try more relaxed matching if spacing is different
    content = re.sub(
        r'// Categoría.*?\n\s*if\s*\(type\s*!=\s*TransactionType\.expense\)\s*\{\s*FinlyPickerField\([^)]*\)\s*\}',
        new_block.strip(),
        content,
        flags=re.DOTALL
    )

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
