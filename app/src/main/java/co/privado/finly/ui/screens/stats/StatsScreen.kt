package co.privado.finly.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.privado.finly.ui.theme.ColorBone
import co.privado.finly.ui.theme.ColorInk
import co.privado.finly.ui.theme.Fraunces

@Composable
fun StatsScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(ColorInk),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Estadísticas",
            style = TextStyle(
                fontFamily = Fraunces,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                color = ColorBone
            )
        )
    }
}
