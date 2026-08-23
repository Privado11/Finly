package co.privado.finly.ui.screens.allowedapps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AllowedAppsScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Apps monitoreadas", style = MaterialTheme.typography.headlineMedium)
        Text("Selecciona qué apps pueden generar transacciones automáticas.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        Text("(Lista de apps instaladas con switch — se implementa en siguiente iteración §4)")
    }
}
