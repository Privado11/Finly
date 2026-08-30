package co.privado.finly.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.CategoryType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        modifier = Modifier,
        topBar = { TopAppBar(title = { Column { Text("Categorías", style = MaterialTheme.typography.titleLarge); Text("Clasifica cada movimiento", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { viewModel.showCreateDialog(true) }, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Nueva categoría") }) }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.categories.isEmpty() -> EmptyCategories(Modifier.fillMaxSize().padding(padding)) { viewModel.showCreateDialog(true) }
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                CategoryType.entries.forEach { type ->
                    val categories = state.categories.filter { it.type == type }
                    if (categories.isNotEmpty()) {
                        item { Text(type.sectionLabel(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        items(categories, key = { it.id }) { CategoryRow(it) }
                    }
                }
            }
        }
    }
    if (state.showCreateDialog) CreateCategoryDialog(state.isSaving, { if (!state.isSaving) viewModel.showCreateDialog(false) }, viewModel::createCategory)
    state.error?.let { AlertDialog(onDismissRequest = viewModel::dismissError, title = { Text("No pudimos completar la acción") }, text = { Text(it) }, confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("Entendido") } }) }
}

@Composable private fun EmptyCategories(modifier: Modifier, onCreate: () -> Unit) = Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(96.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Category, null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) } }
    Spacer(Modifier.height(24.dp)); Text("Crea tus categorías", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(8.dp)); Text("Te ayudarán a entender en qué entra y sale tu dinero.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 36.dp)); Spacer(Modifier.height(24.dp)); Button(onClick = onCreate) { Text("Crear categoría") }
}

@Composable private fun CategoryRow(category: Category) {
    val color = if (category.type == CategoryType.income) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.14f), modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(category.type.icon(), null, tint = color) } }
        Spacer(Modifier.size(14.dp)); Text(category.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f)); Text(category.type.label(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun CreateCategoryDialog(isSaving: Boolean, onDismiss: () -> Unit, onCreate: (String, CategoryType) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }; var type by rememberSaveable { mutableStateOf(CategoryType.expense) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Nueva categoría") }, text = { Column {
        Text("Usa categorías sencillas que reconozcas rápido.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(20.dp)); OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), enabled = !isSaving, label = { Text("Nombre") }, singleLine = true); Spacer(Modifier.height(16.dp)); CategoryType.entries.forEach { option -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { RadioButton(type == option, { type = option }, enabled = !isSaving); Icon(option.icon(), null); Text(option.label(), Modifier.padding(start = 12.dp)) } }
    } }, dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") } }, confirmButton = { Button(onClick = { onCreate(name, type) }, enabled = !isSaving) { if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Guardar") } })
}

private fun CategoryType.label() = if (this == CategoryType.income) "Ingreso" else "Gasto"
private fun CategoryType.sectionLabel() = if (this == CategoryType.income) "Ingresos" else "Gastos"
private fun CategoryType.icon(): ImageVector = if (this == CategoryType.income) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward
