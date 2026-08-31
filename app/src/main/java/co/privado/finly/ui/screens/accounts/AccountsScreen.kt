package co.privado.finly.ui.screens.accounts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import co.privado.finly.ui.theme.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.domain.model.AccountBalance
import co.privado.finly.domain.model.AccountType
import co.privado.finly.ui.navigation.FinlyFab
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: AccountsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.background(ColorInk),
        containerColor = ColorInk,
        floatingActionButton = { 
            FinlyFab(text = "Cuenta") { viewModel.showCreateDialog(true) }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)) {
                Text(
                    text = "CUENTAS",
                    style = TypographyEyebrow,
                    color = ColorBrass,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Tus lugares financieros",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Medium,
                        fontSize = 26.sp,
                        color = ColorBone
                    )
                )
                
                val totalBalance = uiState.accounts.filter { it.active }.sumOf { it.balance }
                val formattedTotal = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
                    maximumFractionDigits = 0
                }.format(totalBalance)
                
                Text(
                    text = "Patrimonio total: $formattedTotal",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        color = ColorSlate
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ColorBrass) }
                uiState.accounts.isEmpty() -> EmptyAccounts(Modifier.fillMaxSize()) { viewModel.showCreateDialog(true) }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.accounts, key = { it.id }) { account -> 
                        AccountCard(
                            account = account, 
                            onEdit = { viewModel.showEditDialog(account) },
                            onDelete = { viewModel.showDeleteDialog(account) }
                        ) 
                    }
                }
            }
        }
    }
    if (uiState.showCreateDialog) {
        CreateAccountDialog(
            isSaving = uiState.isSaving,
            accountToEdit = uiState.accountToEdit,
            onDismiss = { if (!uiState.isSaving) viewModel.showCreateDialog(false) },
            onSave = viewModel::saveAccount
        )
    }
    uiState.accountToDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { if (!uiState.isSaving) viewModel.showDeleteDialog(null) },
            title = { Text("Eliminar cuenta", fontFamily = Fraunces, color = ColorBone) },
            text = { Text("¿Estás seguro de que quieres eliminar la cuenta '${account.name}'? Esta acción no se puede deshacer.", fontFamily = Inter, color = ColorSlate) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAccount(account) },
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorError, contentColor = ColorBone)
                ) {
                    if (uiState.isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = ColorBone)
                    else Text("Eliminar", fontFamily = Inter, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteDialog(null) }, enabled = !uiState.isSaving) {
                    Text("Cancelar", fontFamily = Inter, color = ColorSlate)
                }
            },
            containerColor = ColorSurface
        )
    }
    uiState.error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("Entendido", color = ColorBrass) } },
            title = { Text("Ups...", fontFamily = Fraunces, color = ColorBone) },
            text = { Text(message, fontFamily = Inter, color = ColorSlate) },
            containerColor = ColorSurface,
            titleContentColor = ColorBone,
            textContentColor = ColorSlate
        )
    }
}

@Composable
private fun EmptyAccounts(modifier: Modifier, onCreate: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(shape = RoundedCornerShape(28.dp), color = ColorSurface, modifier = Modifier.size(96.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(44.dp), tint = ColorBrass)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Agrega tu primera cuenta", fontFamily = Fraunces, fontSize = 24.sp, color = ColorBone)
        Spacer(Modifier.height(8.dp))
        Text("Así podrás registrar movimientos y conocer tu balance real.", fontFamily = Inter, fontSize = 15.sp, color = ColorSlate, modifier = Modifier.padding(horizontal = 36.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onCreate,
            colors = ButtonDefaults.buttonColors(containerColor = ColorBrass, contentColor = ColorOnBrass)
        ) { 
            Text("Crear cuenta", fontFamily = Inter, fontWeight = FontWeight.Bold) 
        }
    }
}

@Composable
private fun AccountCard(account: AccountBalance, onEdit: () -> Unit, onDelete: () -> Unit) {
    val formattedBalance = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }.format(account.balance)

    Surface(shape = RoundedCornerShape(20.dp), color = ColorSurface, onClick = onEdit) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = ColorBrass.copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(account.type.icon(), contentDescription = null, tint = ColorBrass)
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(account.name, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = ColorBone)
                Spacer(Modifier.height(2.dp))
                Text(account.type.label(), fontFamily = Inter, fontSize = 13.sp, color = ColorSlate)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formattedBalance, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ColorBrass)
                Text(account.currency, fontFamily = Inter, fontSize = 12.sp, color = ColorSlate)
            }
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = ColorBrass)
            }
        }
    }
}

@Composable
private fun CreateAccountDialog(isSaving: Boolean, accountToEdit: AccountBalance?, onDismiss: () -> Unit, onSave: (String?, String, AccountType, Double) -> Unit) {
    var name by rememberSaveable { mutableStateOf(accountToEdit?.name ?: "") }
    var type by rememberSaveable { mutableStateOf(accountToEdit?.type ?: AccountType.bank) }
    var openingBalanceStr by rememberSaveable { mutableStateOf(accountToEdit?.openingBalance?.toLong()?.toString() ?: "") }
    
    val title = if (accountToEdit == null) "Nueva cuenta" else "Editar cuenta"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurface,
        titleContentColor = ColorBone,
        textContentColor = ColorSlate,
        shape = RoundedCornerShape(24.dp),
        title = { Text(title, fontFamily = Fraunces) },
        text = {
            Column {
                if (accountToEdit == null) {
                    Text("Añade los lugares donde guardas o manejas tu dinero.", fontFamily = Inter)
                    Spacer(Modifier.height(20.dp))
                }
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Nombre de la cuenta", fontFamily = Inter) }, 
                    singleLine = true, 
                    enabled = !isSaving, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorBrass,
                        focusedLabelColor = ColorBrass,
                        unfocusedBorderColor = ColorHair,
                        unfocusedLabelColor = ColorSlate,
                        focusedTextColor = ColorBone,
                        unfocusedTextColor = ColorBone,
                        cursorColor = ColorBrass
                    )
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = openingBalanceStr,
                    onValueChange = { newValue -> 
                        if (type == AccountType.credit_card) {
                            if (newValue.isEmpty() || newValue == "-" || newValue.matches(Regex("^-?\\d*$"))) {
                                openingBalanceStr = newValue
                            }
                        } else {
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*$"))) {
                                openingBalanceStr = newValue
                            }
                        }
                    },
                    label = { Text("Saldo inicial", fontFamily = Inter) },
                    singleLine = true,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorBrass,
                        focusedLabelColor = ColorBrass,
                        unfocusedBorderColor = ColorHair,
                        unfocusedLabelColor = ColorSlate,
                        focusedTextColor = ColorBone,
                        unfocusedTextColor = ColorBone,
                        cursorColor = ColorBrass
                    )
                )
                if (type == AccountType.credit_card) {
                    Text(
                        text = "Ingresa un valor negativo si ya tienes deuda en esta tarjeta.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = ColorSlate,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text("Tipo de cuenta", fontFamily = Inter, fontWeight = FontWeight.SemiBold, color = ColorBone)
                Spacer(Modifier.height(6.dp))
                AccountType.entries.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = type == option, 
                            onClick = { 
                                type = option 
                                if (option != AccountType.credit_card && openingBalanceStr.startsWith("-")) {
                                    openingBalanceStr = "0"
                                }
                            }, 
                            enabled = !isSaving,
                            colors = RadioButtonDefaults.colors(selectedColor = ColorBrass, unselectedColor = ColorSlate)
                        )
                        Icon(option.icon(), contentDescription = null, tint = if (type == option) ColorBrass else ColorSlate, modifier = Modifier.padding(start = 4.dp))
                        Text(option.label(), fontFamily = Inter, color = if (type == option) ColorBone else ColorSlate, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss, enabled = !isSaving) { 
                Text("Cancelar", fontFamily = Inter, color = ColorSlate) 
            } 
        },
        confirmButton = {
            Button(
                onClick = { onSave(accountToEdit?.id, name, type, openingBalanceStr.toDoubleOrNull() ?: 0.0) }, 
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = ColorBrass, contentColor = ColorOnBrass),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = ColorOnBrass)
                else Text("Guardar", fontFamily = Inter, fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun AccountType.label(): String = when (this) {
    AccountType.cash -> "Efectivo"
    AccountType.bank -> "Cuenta bancaria"
    AccountType.credit_card -> "Tarjeta de crédito"
    AccountType.digital_wallet -> "Billetera digital"
}

private fun AccountType.icon(): ImageVector = when (this) {
    AccountType.cash -> Icons.Filled.Payments
    AccountType.bank -> Icons.Filled.AccountBalance
    AccountType.credit_card -> Icons.Filled.CreditCard
    AccountType.digital_wallet -> Icons.Filled.PhoneAndroid
}
