with open('app/src/main/java/co/privado/finly/ui/screens/transactions/Pickers.kt', 'r') as f:
    content = f.read()

# Remove the focus parameter in PickerBuscador
content = content.replace(""",
                onFocusChange = { buscando = it }""", "")
                
content = content.replace(", onFocusChange: (Boolean) -> Unit", "")
content = content.replace(".onFocusChanged { focusState -> onFocusChange(focusState.isFocused) }", "")

# Remove buscando variable
content = content.replace("var buscando by remember { mutableStateOf(false) } // true mientras el campo de búsqueda tiene foco\n", "")
content = content.replace("    var buscando by remember { mutableStateOf(false) } // true mientras el campo de búsqueda tiene foco\n", "")

# Remove the if condition wrapper
old_boton_cuenta = """            // Oculto mientras se está buscando (campo con foco)
            if (!buscando) {
                PickerBotonAgregar("Nueva Cuenta", onAgregarCuenta)
            }"""
new_boton_cuenta = """            // Botón de nueva cuenta visible siempre
            PickerBotonAgregar("Nueva Cuenta", onAgregarCuenta)"""
content = content.replace(old_boton_cuenta, new_boton_cuenta)

old_boton_categoria = """            // Oculto mientras se está buscando (campo con foco)
            if (!buscando) {
                PickerBotonAgregar("Nueva Categoría", onAgregarCategoria)
            }"""
new_boton_categoria = """            // Botón de nueva categoría visible siempre
            PickerBotonAgregar("Nueva Categoría", onAgregarCategoria)"""
content = content.replace(old_boton_categoria, new_boton_categoria)


with open('app/src/main/java/co/privado/finly/ui/screens/transactions/Pickers.kt', 'w') as f:
    f.write(content)
