with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Fix Title
content = content.replace(
    'text = "Nuevo movimiento",',
    'text = if (state.initialTransaction != null) "Editar movimiento" else "Nuevo movimiento",'
)

# Fix Button
content = content.replace(
    'text = "Guardar movimiento",',
    'text = if (state.initialTransaction != null) "Guardar cambios" else "Guardar movimiento",'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
