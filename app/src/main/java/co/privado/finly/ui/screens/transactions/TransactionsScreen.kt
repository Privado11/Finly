package co.privado.finly.ui.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.domain.model.Account
import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(onSaved: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var amount by rememberSaveable { mutableStateOf("") }; var merchant by rememberSaveable { mutableStateOf("") }; var type by rememberSaveable { mutableStateOf(TransactionType.expense) }
    var account by remember { mutableStateOf<Account?>(null) }; var destination by remember { mutableStateOf<Account?>(null) }; var category by remember { mutableStateOf<Category?>(null) }
    var picker by remember { mutableStateOf<String?>(null) }
    Scaffold(modifier = Modifier.systemBarsPadding(), topBar = { TopAppBar(title = { Column { Text("Nuevo movimiento", style = MaterialTheme.typography.titleLarge); Text("Registra tu dinero en segundos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }) }) { padding ->
        if (state.isLoading) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() } else Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Tipo", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) { listOf(TransactionType.expense to "Gasto", TransactionType.income to "Ingreso", TransactionType.transfer to "Transferencia").forEachIndexed { index, item -> SegmentedButton(selected = type == item.first, onClick = { type = item.first; category = null }, shape = SegmentedButtonDefaults.itemShape(index, 3)) { Text(item.second) } } }
            OutlinedTextField(amount, { amount = it }, Modifier.fillMaxWidth(), label = { Text("Monto (COP)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            PickerField("Cuenta", account?.name ?: "Seleccionar cuenta") { picker = "account" }
            if (type == TransactionType.transfer) PickerField("Cuenta destino", destination?.name ?: "Seleccionar cuenta destino") { picker = "destination" }
            if (type != TransactionType.transfer) PickerField("Categoría", category?.name ?: "Sin categoría") { picker = "category" }
            OutlinedTextField(merchant, { merchant = it }, Modifier.fillMaxWidth(), label = { Text(if (type == TransactionType.expense) "Comercio (opcional)" else "Descripción (opcional)") }, singleLine = true)
            Spacer(Modifier.height(8.dp)); Button(onClick = { viewModel.save(amount, type, account?.id, destination?.id, category?.id, merchant, onSaved) }, enabled = !state.isSaving && state.accounts.isNotEmpty(), modifier = Modifier.fillMaxWidth().height(52.dp)) { if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp) else Text("Guardar movimiento") }
        }
    }
    when (picker) { "account" -> AccountPicker("Selecciona una cuenta", state.accounts, { account = it; picker = null }, { picker = null }); "destination" -> AccountPicker("Selecciona la cuenta destino", state.accounts.filter { it.id != account?.id }, { destination = it; picker = null }, { picker = null }); "category" -> CategoryPicker(state.categories.filter { it.type.name == type.name }, { category = it; picker = null }, { picker = null }); null -> Unit }
    state.error?.let { AlertDialog(onDismissRequest = viewModel::dismissError, title = { Text("Revisa el movimiento") }, text = { Text(it) }, confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("Entendido") } }) }
}

@Composable private fun PickerField(label: String, value: String, onClick: () -> Unit) { OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), contentPadding = PaddingValues(horizontal = 16.dp)) { Column(Modifier.weight(1f)) { Text(label, style = MaterialTheme.typography.bodySmall); Text(value, style = MaterialTheme.typography.bodyLarge) } } }
@Composable private fun AccountPicker(title: String, items: List<Account>, select: (Account) -> Unit, dismiss: () -> Unit) = AlertDialog(onDismissRequest = dismiss, title = { Text(title) }, text = { Column { items.forEach { TextButton(onClick = { select(it) }, modifier = Modifier.fillMaxWidth()) { Text(it.name) } } } }, confirmButton = { TextButton(onClick = dismiss) { Text("Cancelar") } })
@Composable private fun CategoryPicker(items: List<Category>, select: (Category) -> Unit, dismiss: () -> Unit) = AlertDialog(onDismissRequest = dismiss, title = { Text("Selecciona una categoría") }, text = { Column { TextButton(onClick = { dismiss() }, modifier = Modifier.fillMaxWidth()) { Text("Sin categoría") }; items.forEach { TextButton(onClick = { select(it) }, modifier = Modifier.fillMaxWidth()) { Text(it.name) } } } }, confirmButton = { TextButton(onClick = dismiss) { Text("Cancelar") } })
