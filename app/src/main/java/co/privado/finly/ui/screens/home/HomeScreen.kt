package co.privado.finly.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.privado.finly.ui.navigation.Routes

@Composable
fun HomeScreen(nav: NavController) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Finly", style = MaterialTheme.typography.headlineLarge)
        Text("Balance general y últimos movimientos", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        // Placeholders — roadmap §7
        Button(onClick = { nav.navigate(Routes.AllowedApps) }, modifier = Modifier.fillMaxWidth()) { Text("Apps monitoreadas") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Ver transacciones") }
    }
}
