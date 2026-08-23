package co.privado.finly.ui.screens.biometric

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BiometricLockScreen(onUnlocked: () -> Unit = {}) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Desbloquear Finly", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("Usa tu huella para continuar")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onUnlocked) { Text("Desbloquear con huella") }
        TextButton(onClick = onUnlocked) { Text("Usar contraseña") }
    }
}
