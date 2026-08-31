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
import androidx.compose.ui.text.input.VisualTransformation
import co.privado.finly.util.CurrencyVisualTransformation
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
@androidx.compose.material3.ExperimentalMaterial3Api
fun TransactionsScreen(initialType: co.privado.finly.domain.model.TransactionType = co.privado.finly.domain.model.TransactionType.expense, onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var amount by rememberSaveable { mutableStateOf("") }
    var merchant by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(initialType) }
    var account by remember { mutableStateOf<co.privado.finly.domain.model.AccountBalance?>(null) }
    var category by remember { mutableStateOf<Category?>(null) }
    var picker by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(state.initialTransaction, state.accounts, state.categories) {
        state.initialTransaction?.let { tx ->
            amount = tx.amount.toLong().toString()
            merchant = tx.merchant ?: ""
            notes = tx.description ?: ""
            type = tx.type
            account = state.accounts.find { it.id == tx.sourceAccountId }
            category = state.categories.find { it.id == tx.categoryId }
        }
    }

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
                            text = "MOVIMIENTO",
                            style = TypographyEyebrow,
                            color = ColorBrass,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = if (state.initialTransaction != null) "Editar movimiento" else "Nuevo movimiento",
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
                if (state.initialTransaction == null) {
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
                }

                // Monto
                FinlyTextField(
                    label = "Monto (COP)",
                    value = amount,
                    onValueChange = { if (it.length <= 12) amount = it.filter { char -> char.isDigit() } },
                    placeholder = "0",
                    isBig = true,
                    keyboardType = KeyboardType.Number,
                    visualTransformation = CurrencyVisualTransformation(),
                    prefix = {
                        Text(
                            text = "$",
                            style = TextStyle(
                                fontFamily = Fraunces,
                                fontSize = 26.sp,
                                color = ColorBrass
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                )

                // Cuenta
                FinlyPickerField(
                    label = "Cuenta",
                    value = account?.name ?: "Seleccionar cuenta",
                    isPlaceholder = account == null,
                    onClick = { picker = "account" }
                )


                // Categoría
                FinlyPickerField(
                    label = "Categoría",
                    value = category?.name ?: "Sin categoría",
                    isPlaceholder = category == null,
                    onClick = { picker = "category" }
                )

                
                // Nota
                FinlyTextField(
                    label = "Nota (opcional)",
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Ej. Almuerzo de trabajo",
                    isBig = false
                )

                // Comercio
                FinlyTextField(
                    label = if (type == TransactionType.expense) "Comercio (opcional)" else "Descripción (opcional)",
                    value = merchant,
                    onValueChange = { merchant = it },
                    placeholder = when (type) {
                        TransactionType.expense -> "Ej. Rappi, Éxito..."
                        TransactionType.income -> "Ej. Quincena..."
                    },
                    isBig = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                val canSave = !state.isSaving && account != null && category != null && amount.isNotBlank() && amount != "0"

                // Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (!canSave) ColorSurfaceHi else ColorBrass)
                        .clickable(enabled = canSave) {
                            viewModel.save(amount, type, account?.id, null, category?.id, merchant, notes.takeIf { it.isNotBlank() }, onSaved)
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ColorSlate, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (state.initialTransaction != null) "Guardar cambios" else "Guardar movimiento",
                            style = TextStyle(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (!canSave) ColorSlate else Color(0xFF1A1305)
                            )
                        )
                    }
                }
            }
        }
    }

    when (picker) {
        "account" -> AccountPicker(title = "Selecciona una cuenta", items = state.accounts, select = { account = it; picker = null }, dismiss = { picker = null })
        "category" -> CategoryPicker(items = state.categories.filter { it.type.name == type.name }, select = { category = it; picker = null }, dismiss = { picker = null })
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
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    prefix: @Composable (() -> Unit)? = null
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
                visualTransformation = visualTransformation,
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (prefix != null) {
                            prefix()
                        }
                        Box(modifier = Modifier.weight(1f)) {
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
                    }
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

