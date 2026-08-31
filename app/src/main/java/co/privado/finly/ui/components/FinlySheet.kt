package co.privado.finly.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.privado.finly.ui.theme.ColorHair
import co.privado.finly.ui.theme.ColorInk
import kotlinx.coroutines.launch

/**
 * Bottom sheet que:
 * - Abre al [alturaInicial] (fracción de la pantalla, ej. 0.6f = 60%)
 * - Se puede expandir hasta [alturaExpandida] SOLO arrastrando el drag handle,
 *   siguiendo el dedo en tiempo real (no un salto animado al soltar el gesto)
 * - Al soltar, se "asienta" (snap) al estado más cercano de los dos
 * - El contenido interno (ej. una lista) hace su propio scroll normal,
 *   sin competir nunca con el gesto del sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinlySheet(
    onDismissRequest: () -> Unit,
    alturaInicial: Float = 0.6f,
    alturaExpandida: Float = 0.95f,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Fracción de pantalla actual (0.6 a 0.95). Es un Animatable para poder
    // moverlo 1:1 con el dedo (snapTo) y animarlo suavemente solo al soltar (animateTo).
    val fraccion = remember { Animatable(alturaInicial) }

    val alturaActual: Dp = screenHeightDp * fraccion.value

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        sheetGesturesEnabled = false, // el sheet nativo NO reacciona a ningún drag; todo lo maneja nuestro handle
        dragHandle = null,
        containerColor = ColorInk,
        contentWindowInsets = { WindowInsets(0) }
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(alturaActual)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                DragHandle(
                    onDragDelta = { deltaPx ->
                        // Convierte el delta de arrastre (px) a fracción de pantalla, y lo suma AHORA MISMO
                        val deltaFraccion = with(density) { deltaPx.toDp() } / screenHeightDp
                        val nuevaFraccion = (fraccion.value - deltaFraccion)
                            .coerceIn(alturaInicial, alturaExpandida)
                        scope.launch { fraccion.snapTo(nuevaFraccion) } // sigue el dedo, sin animación de por medio
                    },
                    onDragEnd = {
                        // Al soltar: se asienta al estado más cercano de los dos
                        val medio = (alturaInicial + alturaExpandida) / 2f
                        val destino = if (fraccion.value >= medio) alturaExpandida else alturaInicial
                        scope.launch { fraccion.animateTo(destino, tween(durationMillis = 200)) }
                    }
                )
                Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DragHandle(onDragDelta: (Float) -> Unit, onDragEnd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        onDragDelta(dragAmount) // se llama en CADA frame de movimiento, no al final
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .background(ColorHair, RoundedCornerShape(2.dp))
        )
    }
}
