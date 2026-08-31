package co.privado.finly.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.util.toIcon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import co.privado.finly.ui.theme.ColorSurfaceHi
import co.privado.finly.domain.model.Category
import co.privado.finly.util.toIcon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import co.privado.finly.ui.theme.ColorSurfaceHi
import co.privado.finly.domain.model.CategoryType
import co.privado.finly.ui.navigation.FinlyFab
import co.privado.finly.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun CategoriesScreen(viewModel: CategoriesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        modifier = Modifier.background(ColorInk),
        containerColor = ColorInk,
        floatingActionButton = { FinlyFab("Categoría") { viewModel.showCreateDialog(true) } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)) {
                Text(
                    text = "CATEGORÍAS",
                    style = TypographyEyebrow,
                    color = ColorBrass,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Organiza tu dinero",
                    style = TextStyle(
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Medium,
                        fontSize = 26.sp,
                        color = ColorBone
                    )
                )
            }
            
            var selectedTab by rememberSaveable { mutableStateOf(CategoryType.expense) }
            
            // Segmented control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorSurface)
                    .border(1.dp, ColorHair, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                val options = listOf(
                    CategoryType.expense to "Gastos",
                    CategoryType.income to "Ingresos",
                )
                options.forEach { (optType, label) ->
                    val selected = selectedTab == optType
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) ColorBrass else Color.Transparent)
                            .clickable { selectedTab = optType }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selected) Color(0xFF1A1305) else ColorSlate
                            )
                        )
                    }
                }
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ColorBrass) }
                state.categories.isEmpty() -> EmptyCategories(Modifier.fillMaxSize()) { viewModel.showCreateDialog(true) }
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 0.dp, 20.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val mainCategories = state.categories.filter { it.type == selectedTab && it.parentId == null }
                    if (mainCategories.isNotEmpty()) {
                        mainCategories.forEach { parent ->
                            item(key = parent.id) { 
                                Text(parent.name, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = ColorBone, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                            }
                            val subcategories = state.categories.filter { it.parentId == parent.id }
                            items(subcategories, key = { it.id }) { category -> 
                                CategoryCard(category, onDelete = { viewModel.showDeleteDialog(category) }) 
                            }
                        }
                    }
                }
            }
        }
    }
    if (state.showCreateDialog) CreateCategoryDialog(state.isSaving, state.categories, { if (!state.isSaving) viewModel.showCreateDialog(false) }, viewModel::createCategory)
    
    state.categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { if (!state.isSaving) viewModel.showDeleteDialog(null) },
            title = { Text("Eliminar categoría", fontFamily = Fraunces, color = ColorBone) },
            text = { Text("¿Estás seguro de que quieres eliminar '${category.name}'? Esta acción no se puede deshacer.", fontFamily = Inter, color = ColorSlate) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteCategory(category) },
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorError, contentColor = ColorBone)
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = ColorBone)
                    else Text("Eliminar", fontFamily = Inter, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteDialog(null) }, enabled = !state.isSaving) {
                    Text("Cancelar", fontFamily = Inter, color = ColorSlate)
                }
            },
            containerColor = ColorSurface
        )
    }
    
    state.error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("Ups...", fontFamily = Fraunces, color = ColorBone) },
            text = { Text(message, fontFamily = Inter, color = ColorSlate) },
            confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("Entendido", color = ColorBrass) } },
            containerColor = ColorSurface, titleContentColor = ColorBone, textContentColor = ColorSlate
        )
    }
}

@Composable private fun EmptyCategories(modifier: Modifier, onCreate: () -> Unit) = Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Surface(shape = RoundedCornerShape(28.dp), color = ColorSurface, modifier = Modifier.size(96.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Category, null, modifier = Modifier.size(44.dp), tint = ColorBrass) } }
    Spacer(Modifier.height(24.dp)); Text("Crea tus categorías", fontFamily = Fraunces, fontSize = 24.sp, color = ColorBone); Spacer(Modifier.height(8.dp)); Text("Te ayudarán a entender en qué entra y sale tu dinero.", fontFamily = Inter, fontSize = 15.sp, color = ColorSlate, modifier = Modifier.padding(horizontal = 36.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center); Spacer(Modifier.height(24.dp))
    Button(onClick = onCreate, colors = ButtonDefaults.buttonColors(containerColor = ColorBrass, contentColor = ColorOnBrass)) { Text("Crear categoría", fontFamily = Inter, fontWeight = FontWeight.Bold) }
}

@Composable private fun CategoryCard(category: Category, onDelete: () -> Unit) {
    val color = if (category.type == CategoryType.income) ColorMoss else ColorClay
    Surface(shape = RoundedCornerShape(16.dp), color = ColorSurface) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(category.icon.toIcon(), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(category.name, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = ColorBone)
            }
            if (category.userId != null) {
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = ColorSlate, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable private fun CreateCategoryDialog(isSaving: Boolean, categories: List<Category>, onDismiss: () -> Unit, onCreate: (String, CategoryType, String?, String?) -> Unit) {
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
                
                var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
                
                Box(modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }) {
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
                        modifier = Modifier
                            .background(ColorSurfaceHi)
                            .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
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
}

private fun CategoryType.label() = if (this == CategoryType.income) "Ingreso" else "Gasto"
private fun CategoryType.sectionLabel() = if (this == CategoryType.income) "Ingresos" else "Gastos"
private fun CategoryType.icon(): ImageVector = if (this == CategoryType.income) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward
