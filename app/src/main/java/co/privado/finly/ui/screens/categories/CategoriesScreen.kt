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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            Text("CATEGORÍAS", style = TypographyEyebrow, color = ColorBrass, modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 4.dp, top = 24.dp))
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ColorBrass) }
                state.categories.isEmpty() -> EmptyCategories(Modifier.fillMaxSize()) { viewModel.showCreateDialog(true) }
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CategoryType.values().forEach { type ->
                        val mainCategories = state.categories.filter { it.type == type && it.parentId == null }
                        if (mainCategories.isNotEmpty()) {
                            item { Text(type.sectionLabel(), fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ColorSlate) }
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
    Surface(shape = RoundedCornerShape(20.dp), color = ColorSurface) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(category.icon.toIcon(), contentDescription = null, tint = color)
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(category.name, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = ColorBone)
            }
            if (category.userId != null) {
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = ColorSlate)
                }
            }
        }
    }
}

@Composable private fun CreateCategoryDialog(isSaving: Boolean, categories: List<Category>, onDismiss: () -> Unit, onCreate: (String, CategoryType, String?, String?) -> Unit) {
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
}

private fun CategoryType.label() = if (this == CategoryType.income) "Ingreso" else "Gasto"
private fun CategoryType.sectionLabel() = if (this == CategoryType.income) "Ingresos" else "Gastos"
private fun CategoryType.icon(): ImageVector = if (this == CategoryType.income) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward
