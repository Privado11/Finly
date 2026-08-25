package co.privado.finly.ui.screens.biometric

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import co.privado.finly.util.BiometricHelper
import androidx.compose.material3.Icon

@Composable
fun BiometricLockScreen(
    onUnlocked: () -> Unit = {},
    onUsePassword: () -> Unit = {}
) {
    val activity = LocalContext.current as? FragmentActivity
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var canAuth by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        canAuth = activity?.let(BiometricHelper::puedeAutenticar) ?: false
        if (canAuth) triggerBiometric(activity, onUnlocked) { errorMsg = it }
    }

    Box(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(112.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Text("Tu información está protegida", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (canAuth) "Confirma tu identidad para acceder a Finly." else "No encontramos una huella configurada en este dispositivo.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            if (canAuth) {
                Button(onClick = { triggerBiometric(activity, onUnlocked) { errorMsg = it } }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Desbloquear con huella")
                }
                Spacer(Modifier.height(12.dp))
            }
            OutlinedButton(onClick = onUsePassword, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Usar contraseña")
            }
            errorMsg?.let {
                Spacer(Modifier.height(20.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun triggerBiometric(
    activity: FragmentActivity?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (activity == null) {
        onError("No se pudo iniciar la autenticación biométrica.")
        return
    }
    BiometricHelper.mostrarPrompt(
        activity = activity,
        onExito = onSuccess,
        onFallo = { onError("Huella no reconocida. Intenta de nuevo o usa tu contraseña.") }
    )
}
