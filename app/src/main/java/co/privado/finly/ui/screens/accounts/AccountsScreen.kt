package co.privado.finly.ui.screens.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccountsScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) { Text("Accounts", style = MaterialTheme.typography.headlineMedium) }
}
