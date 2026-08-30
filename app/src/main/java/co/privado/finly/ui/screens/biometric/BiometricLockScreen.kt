package co.privado.finly.ui.screens.biometric

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import co.privado.finly.ui.components.auth.PrimaryAuthButton
import co.privado.finly.ui.theme.ColorBone
import co.privado.finly.ui.theme.ColorBrass
import co.privado.finly.ui.theme.ColorHair
import co.privado.finly.ui.theme.ColorInk
import co.privado.finly.ui.theme.ColorSlate
import co.privado.finly.ui.theme.Fraunces
import co.privado.finly.ui.theme.Inter
import co.privado.finly.util.BiometricHelper

/**
 * Pantalla de desbloqueo biométrico.
 * Diseño según finly-design-system.html §06:
 *   - Anillo circular con arco en ColorBrass (78% del círculo)
 *   - Icono de huella centrado en ColorBrass
 *   - Título Fraunces "Hola de nuevo, <nombre>"
 *   - Subtítulo Inter slate
 *   - Link "Usar contraseña en su lugar" en brass
 */
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
        modifier = Modifier
            .fillMaxSize()
            .background(ColorInk)
            .systemBarsPadding()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Anillo con arco en brass ─────────────────────────────────────
            // Replica: .lock-ring con ::before conic-gradient brass 78%
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .drawWithCache {
                        onDrawBehind {
                            val strokeWidth = 3.dp.toPx()
                            val inset = strokeWidth / 2f
                            val arcRect = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(inset, inset)

                            // Pista completa — ColorHair
                            drawArc(
                                color = ColorHair,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcRect,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Arco activo — brass (78% de 360° = 280.8°)
                            drawArc(
                                brush = Brush.sweepGradient(
                                    0f to ColorBrass,
                                    0.78f to ColorBrass,
                                    0.78f to Color.Transparent,
                                    1f to Color.Transparent
                                ),
                                startAngle = -90f,
                                sweepAngle = 280.8f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcRect,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Icono de huella dentro del anillo
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = "Huella digital",
                    modifier = Modifier.size(56.dp),
                    tint = ColorBrass
                )
            }

            Spacer(Modifier.height(26.dp))

            // ── Título — Fraunces 20sp ───────────────────────────────────────
            Text(
                text = "Hola de nuevo",
                style = TextStyle(
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    color = ColorBone
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // ── Subtítulo — Inter slate ──────────────────────────────────────
            Text(
                text = if (canAuth)
                    "Usa tu huella para continuar"
                else
                    "No encontramos una huella configurada en este dispositivo.",
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 12.5.sp,
                    color = ColorSlate,
                    lineHeight = 18.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(34.dp))

            // ── Botón principal (solo si hay huella disponible) ─────────────
            if (canAuth) {
                PrimaryAuthButton(
                    text = "Desbloquear con huella",
                    loading = false,
                    onClick = { triggerBiometric(activity, onUnlocked) { errorMsg = it } }
                )
                Spacer(Modifier.height(20.dp))
            }

            // ── "Usar contraseña en su lugar" — texto brass clickable ────────
            Text(
                text = "Usar contraseña en su lugar",
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    color = ColorBrass
                ),
                modifier = Modifier
                    .clickable(onClick = onUsePassword)
                    .padding(vertical = 8.dp)
            )

            // ── Mensaje de error (huella no reconocida) ──────────────────────
            errorMsg?.let { msg ->
                Spacer(Modifier.height(20.dp))
                Text(
                    text = msg,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = Color(0xFFBE7B62) // ColorClay directo para no importar extra
                    ),
                    textAlign = TextAlign.Center
                )
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
