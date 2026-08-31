with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.ui.layout.onGloballyPositioned" not in content:
    content = content.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.Alignment\nimport androidx.compose.ui.layout.onGloballyPositioned\nimport androidx.compose.ui.unit.toSize\nimport androidx.compose.ui.platform.LocalDensity")

old_box = """                val selectedParent = validParents.find { it.id == parentId }
                
                Box(modifier = Modifier.fillMaxWidth()) {"""

new_box = """                val selectedParent = validParents.find { it.id == parentId }
                
                var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
                
                Box(modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }) {"""

content = content.replace(old_box, new_box)

old_dropdown = """                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(ColorSurfaceHi)
                    )"""

new_dropdown = """                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(ColorSurfaceHi)
                            .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    )"""

content = content.replace(old_dropdown, new_dropdown)

with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'w') as f:
    f.write(content)
