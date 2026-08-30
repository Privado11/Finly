package co.privado.finly.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.domain.model.Account
import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.TransactionType
import co.privado.finly.ui.theme.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun TransactionsScreen(onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var amount by rememberSaveable { mutableStateOf("") }
    var merchant by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(TransactionType.expense) }
    var account by remember { mutableStateOf<Account?>(null) }
    var destination by remember { mutableStateOf<Account?>(null) }
    var category by remember { mutableStateOf<Category?>(null) }
    var picker by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(ColorInk)) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorBrass)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Topbar
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ColorBone
                        )
                    }
                    Column {
                        Text(
                            text = "Nuevo movimiento",
                            style = TextStyle(
                                fontFamily = Fraunces,
                                fontWeight = FontWeight.Medium,
                                fontSize = 26.sp,
                                color = ColorBone
                            )
                        )
                        Text(
                            text = "Registra tu dinero en segundos",
                            style = TextStyle(
                                fontSize = 12.5.sp,
                                color = ColorSlate
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Tipo
                Text(
                    text = "TIPO",
                    style = TypographyEyebrow,
                    color = ColorBrass,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                
                // Segmented control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 22.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorSurface)
                        .border(1.dp, ColorHair, RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    val options = listOf(
                        TransactionType.expense to "Gasto",
                        TransactionType.income to "Ingreso",
                        TransactionType.transfer to "Transferencia"
                    )
                    options.forEach { (optType, label) ->
                        val selected = type == optType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) ColorBrass else Color.Transparent)
                                .clickable {
                                    if (type != optType) {
                                        type = optType
                                        category = null
                                        account = null
                                        destination = null
                                    }
                                }
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

                // Monto
                FinlyTextField(
                    label = "Monto (COP)",
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = "$0",
                    isBig = true,
                    keyboardType = KeyboardType.Decimal
                )

                // Cuenta
                FinlyPickerField(
                    label = "Cuenta",
                    value = account?.name ?: "Seleccionar cuenta",
                    isPlaceholder = account == null,
                    onClick = { picker = "account" }
                )

                // Destino (si es transferencia)
                if (type == TransactionType.transfer) {
                    FinlyPickerField(
                        label = "Cuenta destino",
                        value = destination?.name ?: "Seleccionar cuenta destino",
                        isPlaceholder = destination == null,
                        onClick = { picker = "destination" }
                    )
                }

                // Categoría (si no es transferencia)
                if (type != TransactionType.transfer) {
                    FinlyPickerField(
                        label = "Categoría",
                        value = category?.name ?: "Sin categoría",
                        isPlaceholder = category == null,
                        onClick = { picker = "category" }
                    )
                }

                // Comercio
                FinlyTextField(
                    label = if (type == TransactionType.expense) "Comercio (opcional)" else "Descripción (opcional)",
                    value = merchant,
                    onValueChange = { merchant = it },
                    placeholder = when (type) {
                        TransactionType.expense -> "Ej. Rappi, Éxito..."
                        TransactionType.income -> "Ej. Quincena..."
                        TransactionType.transfer -> "Ej. Transferencia a bolsillo"
                    },
                    isBig = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (state.isSaving || state.accounts.isEmpty()) ColorSurfaceHi else ColorBrass)
                        .clickable(enabled = !state.isSaving && state.accounts.isNotEmpty()) {
                            viewModel.save(amount, type, account?.id, destination?.id, category?.id, merchant, onSaved)
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ColorSlate, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Guardar movimiento",
                            style = TextStyle(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.accounts.isEmpty()) ColorSlate else Color(0xFF1A1305)
                            )
                        )
                    }
                }
            }
        }
    }

    when (picker) {
        "account" -> AccountPicker("Selecciona una cuenta", state.accounts, { account = it; picker = null }, { picker = null })
        "destination" -> AccountPicker("Selecciona la cuenta destino", state.accounts.filter { it.id != account?.id }, { destination = it; picker = null }, { picker = null })
        "category" -> CategoryPicker(state.categories.filter { it.type.name == type.name }, { category = it; picker = null }, { picker = null })
        null -> Unit
    }
    
    state.error?.let { 
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("Revisa el movimiento") },
            text = { Text(it) },
            confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("Entendido") } }
        ) 
    }
}

@Composable
fun FinlyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isBig: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurface)
            .border(1.dp, ColorHair, RoundedCornerShape(16.dp))
            .padding(horizontal = 17.dp, vertical = 15.dp)
    ) {
        Column {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = IbmPlexMono,
                    fontSize = 10.5.sp,
                    letterSpacing = 0.07.sp,
                    color = ColorSlate
                ),
                modifier = Modifier.padding(bottom = 5.dp)
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontFamily = if (isBig) Fraunces else Inter,
                    fontSize = if (isBig) 26.sp else 15.sp,
                    color = ColorBone
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                cursorBrush = SolidColor(ColorBrass),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontFamily = if (isBig) Fraunces else Inter,
                                fontSize = if (isBig) 26.sp else 15.sp,
                                color = Color(0xFF4C555F)
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun FinlyPickerField(
    label: String,
    value: String,
    isPlaceholder: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurface)
            .border(1.dp, ColorHair, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 17.dp, vertical = 15.dp)
    ) {
        Column {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = IbmPlexMono,
                    fontSize = 10.5.sp,
                    letterSpacing = 0.07.sp,
                    color = ColorSlate
                ),
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    color = if (isPlaceholder) Color(0xFF4C555F) else ColorBone
                )
            )
        }
    }
}

@Composable
private fun AccountPicker(title: String, items: List<Account>, select: (Account) -> Unit, dismiss: () -> Unit) = 
    AlertDialog(
        onDismissRequest = dismiss, 
        title = { Text(title) }, 
        text = { 
            Column { 
                items.forEach { 
                    TextButton(onClick = { select(it) }, modifier = Modifier.fillMaxWidth()) { Text(it.name) } 
                } 
            } 
        }, 
        confirmButton = { TextButton(onClick = dismiss) { Text("Cancelar") } }
    )

@Composable
private fun CategoryPicker(items: List<Category>, select: (Category) -> Unit, dismiss: () -> Unit) = 
    AlertDialog(
        onDismissRequest = dismiss, 
        title = { Text("Selecciona una categoría") }, 
        text = { 
            Column { 
                TextButton(onClick = { dismiss() }, modifier = Modifier.fillMaxWidth()) { Text("Sin categoría") }
                items.forEach { 
                    TextButton(onClick = { select(it) }, modifier = Modifier.fillMaxWidth()) { Text(it.name) } 
                } 
            } 
        }, 
        confirmButton = { TextButton(onClick = dismiss) { Text("Cancelar") } }
    )
