package co.privado.finly.ui.screens.accounts

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.domain.model.Account
import co.privado.finly.domain.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: AccountsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cuentas", style = MaterialTheme.typography.titleLarge)
                        Text("Organiza dónde está tu dinero", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nueva cuenta") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = { viewModel.showCreateDialog(true) }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            uiState.accounts.isEmpty() -> EmptyAccounts(Modifier.fillMaxSize().padding(padding)) { viewModel.showCreateDialog(true) }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Tus cuentas activas", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(uiState.accounts, key = { it.id }) { account -> AccountCard(account) }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreateAccountDialog(
            isSaving = uiState.isSaving,
            onDismiss = { if (!uiState.isSaving) viewModel.showCreateDialog(false) },
            onCreate = viewModel::createAccount
        )
    }
    uiState.error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("Entendido") } },
            title = { Text("No pudimos completar la acción") },
            text = { Text(message) }
        )
    }
}

@Composable
private fun EmptyAccounts(modifier: Modifier, onCreate: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(96.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Agrega tu primera cuenta", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Así podrás registrar movimientos y conocer tu balance real.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 36.dp))
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreate) { Text("Crear cuenta") }
    }
}

@Composable
private fun AccountCard(account: Account) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(account.type.icon(), contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(account.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(2.dp))
                Text(account.type.label(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(account.currency, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun CreateAccountDialog(isSaving: Boolean, onDismiss: () -> Unit, onCreate: (String, AccountType) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(AccountType.bank) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva cuenta") },
        text = {
            Column {
                Text("Añade los lugares donde guardas o manejas tu dinero.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre de la cuenta") }, singleLine = true, enabled = !isSaving, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(20.dp))
                Text("Tipo de cuenta", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                AccountType.entries.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = type == option, onClick = { type = option }, enabled = !isSaving)
                        Icon(option.icon(), contentDescription = null, modifier = Modifier.padding(start = 4.dp))
                        Text(option.label(), modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") } },
        confirmButton = {
            Button(onClick = { onCreate(name, type) }, enabled = !isSaving) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Guardar")
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
