import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Replace the when(picker) block with the correct one
old_when = """    when (picker) {
        "account" -> AccountPicker("Selecciona una cuenta", state.accounts, { account = it; picker = null }, { picker = null })
        "destination" -> AccountPicker("Selecciona la cuenta destino", state.accounts.filter { it.id != account?.id }, { destination = it; picker = null }, { picker = null })
        "category" -> CategoryPicker(state.categories.filter { it.type.name == type.name }, { category = it; picker = null }, { picker = null })
        null -> Unit
    }"""
    
new_when = """    when (picker) {
        "account" -> AccountPicker(title = "Selecciona una cuenta", items = state.accounts, select = { account = it; picker = null }, dismiss = { picker = null })
        "category" -> CategoryPicker(items = state.categories.filter { it.type.name == type.name }, select = { category = it; picker = null }, dismiss = { picker = null })
        null -> Unit
    }"""

if old_when in content:
    content = content.replace(old_when, new_when)
else:
    # Use regex if spacing differs
    content = re.sub(r'when\s*\(picker\)\s*\{.*?null\s*->\s*Unit\s*\}', new_when, content, flags=re.DOTALL)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
