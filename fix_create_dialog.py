with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'r') as f:
    content = f.read()

# Make sure DropdownMenu and DropdownMenuItem are imported
if "DropdownMenu" not in content:
    content = content.replace("import androidx.compose.material3.TextButton", "import androidx.compose.material3.TextButton\nimport androidx.compose.material3.DropdownMenu\nimport androidx.compose.material3.DropdownMenuItem")

if "Icons.Filled.ArrowDropDown" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Category", "import androidx.compose.material.icons.filled.Category\nimport androidx.compose.material.icons.filled.ArrowDropDown")


old_dialog = """@Composable private fun CreateCategoryDialog(isSaving: Boolean, categories: List<Category>, onDismiss: () -> Unit, onCreate: (String, CategoryType, String?, String?) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(CategoryType.expense) }
    var parentId by rememberSaveable { mutableStateOf<String?>(null) }
    
    androidx.compose.runtime.LaunchedEffect(type) {
        val validParents = categories.filter { it.type == type && it.parentId == null }
        if (validParents.none { it.id == parentId }) parentId = validParents.firstOrNull()?.id
    }
    
    AlertDialog(
        onDismissRequest = onDismiss, 
        containerColor = ColorSurface,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Nueva categoría", fontFamily = Fraunces, color = ColorBone) }, 
        text = { 
            Column {
                Text("Crea una subcategoría y asígnala a un grupo.", fontFamily = Inter, color = ColorSlate)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    name, { name = it }, Modifier.fillMaxWidth(), enabled = !isSaving, label = { Text("Nombre", fontFamily = Inter) }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBrass, focusedLabelColor = ColorBrass, unfocusedBorderColor = ColorHair, unfocusedLabelColor = ColorSlate, focusedTextColor = ColorBone, unfocusedTextColor = ColorBone, cursorColor = ColorBrass)
                )
                Spacer(Modifier.height(16.dp))
                
                val validParents = categories.filter { it.type == type && it.parentId == null }
                Text("Grupo principal", fontFamily = Inter, fontSize = 13.sp, color = ColorSlate, modifier = Modifier.padding(bottom = 8.dp))
                Column(Modifier.fillMaxWidth().background(ColorSurfaceHi, RoundedCornerShape(12.dp)).padding(8.dp)) {
                    validParents.forEach { parent ->
                        Row(Modifier.fillMaxWidth().clickable(enabled = !isSaving) { parentId = parent.id }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(parentId == parent.id, { parentId = parent.id }, enabled = !isSaving, colors = RadioButtonDefaults.colors(selectedColor = ColorBrass, unselectedColor = ColorSlate))
                            Text(parent.name, fontFamily = Inter, color = if (parentId == parent.id) ColorBone else ColorSlate, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                CategoryType.entries.forEach { option -> 
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { 
                        RadioButton(type == option, { type = option }, enabled = !isSaving, colors = RadioButtonDefaults.colors(selectedColor = ColorBrass, unselectedColor = ColorSlate))
                        Icon(option.icon(), null, tint = if (type == option) ColorBrass else ColorSlate)
                        Text(option.label(), fontFamily = Inter, color = if (type == option) ColorBone else ColorSlate, modifier = Modifier.padding(start = 12.dp)) 
                    } 
                }
            } 
        }, 
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar", fontFamily = Inter, color = ColorSlate) } }, 
        confirmButton = { 
            Button(onClick = { onCreate(name, type, parentId, "Category") }, enabled = !isSaving, colors = ButtonDefaults.buttonColors(containerColor = ColorBrass, contentColor = ColorOnBrass), shape = RoundedCornerShape(12.dp)) { 
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = ColorOnBrass) else Text("Guardar", fontFamily = Inter, fontWeight = FontWeight.Bold) 
            } 
        }
    )
}"""

new_dialog = """@Composable private fun CreateCategoryDialog(isSaving: Boolean, categories: List<Category>, onDismiss: () -> Unit, onCreate: (String, CategoryType, String?, String?) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(CategoryType.expense) }
    var parentId by rememberSaveable { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    androidx.compose.runtime.LaunchedEffect(type) {
        val validParents = categories.filter { it.type == type && it.parentId == null }
        if (validParents.none { it.id == parentId }) {
            parentId = validParents.find { it.name.equals("Otros", ignoreCase = true) }?.id ?: validParents.firstOrNull()?.id
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss, 
        containerColor = ColorSurface,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Nueva categoría", fontFamily = Fraunces, color = ColorBone) }, 
        text = { 
            Column {
                // Segmented Control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorInk)
                        .border(1.dp, ColorHair, RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    val options = listOf(CategoryType.expense to "Gasto", CategoryType.income to "Ingreso")
                    options.forEach { (optType, label) ->
                        val selected = type == optType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) ColorBrass else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable(enabled = !isSaving) { type = optType }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (selected) Color(0xFF1A1305) else ColorSlate)
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    modifier = Modifier.fillMaxWidth(), 
                    enabled = !isSaving, 
                    label = { Text("Nombre", fontFamily = Inter) }, 
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorBrass, focusedLabelColor = ColorBrass, unfocusedBorderColor = ColorHair, unfocusedLabelColor = ColorSlate, focusedTextColor = ColorBone, unfocusedTextColor = ColorBone, cursorColor = ColorBrass)
                )
                Spacer(Modifier.height(16.dp))
                
                val validParents = categories.filter { it.type == type && it.parentId == null }
                val selectedParent = validParents.find { it.id == parentId }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedParent?.name ?: "Seleccionar...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grupo principal", fontFamily = Inter) },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, tint = ColorSlate) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorHair, unfocusedBorderColor = ColorHair, focusedLabelColor = ColorSlate, unfocusedLabelColor = ColorSlate, focusedTextColor = ColorBone, unfocusedTextColor = ColorBone)
                    )
                    Box(modifier = Modifier.matchParentSize().clickable(enabled = !isSaving) { expanded = true })
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(ColorSurfaceHi)
                    ) {
                        validParents.forEach { parent ->
                            DropdownMenuItem(
                                text = { Text(parent.name, color = ColorBone, fontFamily = Inter) },
                                onClick = { parentId = parent.id; expanded = false }
                            )
                        }
                    }
                }
            } 
        }, 
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar", fontFamily = Inter, color = ColorSlate) } }, 
        confirmButton = { 
            Button(onClick = { onCreate(name, type, parentId, "Category") }, enabled = !isSaving, colors = ButtonDefaults.buttonColors(containerColor = ColorBrass, contentColor = ColorOnBrass), shape = RoundedCornerShape(12.dp)) { 
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = ColorOnBrass) else Text("Guardar", fontFamily = Inter, fontWeight = FontWeight.Bold) 
            } 
        }
    )
}"""

content = content.replace(old_dialog, new_dialog)

with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'w') as f:
    f.write(content)
