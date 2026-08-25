package co.privado.finly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FinlyGreenDark,
    onPrimary = Color(0xFF003827),
    primaryContainer = FinlyGreenContainerDark,
    onPrimaryContainer = Color(0xFF8CF8C7),
    secondary = Color(0xFFB5CCC0),
    onSecondary = Color(0xFF20352C),
    secondaryContainer = Color(0xFF364B40),
    onSecondaryContainer = Color(0xFFD1E8DC),
    background = CanvasDark,
    onBackground = InkDark,
    surface = CanvasDark,
    onSurface = InkDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC1CAC3),
    outline = OutlineDark,
    error = FinlyErrorDark,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = FinlyGreen,
    onPrimary = Color.White,
    primaryContainer = FinlyGreenContainer,
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF4C6358),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFE9DA),
    onSecondaryContainer = Color(0xFF092016),
    background = Canvas,
    onBackground = Ink,
    surface = Canvas,
    onSurface = Ink,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Color(0xFF404943),
    outline = Outline,
    error = FinlyError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun FinlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
