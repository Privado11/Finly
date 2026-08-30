package co.privado.finly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ─────────────────────────────────────────────
//  Finly Theme — paleta del design system
//
//  La app es dark-first (fondo #05070A / Ink, surfaces en #1A2028).
//  El lightColorScheme existe como respaldo si el sistema fuerza modo claro,
//  pero la experiencia principal está en oscuro.
// ─────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    // ── Primario: Latón ──────────────────────────────────────────────────────
    primary = ColorBrass,
    onPrimary = ColorOnBrass,
    primaryContainer = ColorBrassSoft,
    onPrimaryContainer = ColorBone,

    // ── Secundario: Slate (acciones neutras, chips, labels) ──────────────────
    secondary = ColorSlate,
    onSecondary = ColorInk,
    secondaryContainer = ColorSurfaceHi,
    onSecondaryContainer = ColorBone,

    // ── Terciario: Musgo (ingresos) ──────────────────────────────────────────
    tertiary = ColorMoss,
    onTertiary = ColorInk,
    tertiaryContainer = ColorSurface,
    onTertiaryContainer = ColorMoss,

    // ── Fondos y superficies ─────────────────────────────────────────────────
    background = ColorInk,
    onBackground = ColorBone,
    surface = ColorSurface,
    onSurface = ColorBone,
    surfaceVariant = ColorSurfaceHi,
    onSurfaceVariant = ColorSlate,

    // ── Contornos ────────────────────────────────────────────────────────────
    outline = ColorHair,
    outlineVariant = ColorSurfaceHi,

    // ── Error ────────────────────────────────────────────────────────────────
    error = ColorError,
    onError = ColorOnError,
    errorContainer = ColorSurface,
    onErrorContainer = ColorError,
)

// Modo claro — misma paleta pero invertida para legibilidad en luz
private val LightColorScheme = lightColorScheme(
    primary = ColorBrassSoft,
    onPrimary = ColorBone,
    primaryContainer = ColorBrass,
    onPrimaryContainer = ColorOnBrass,

    secondary = ColorSlate,
    onSecondary = ColorBone,
    secondaryContainer = ColorSurface,
    onSecondaryContainer = ColorBone,

    tertiary = ColorMoss,
    onTertiary = ColorBone,
    tertiaryContainer = ColorSurface,
    onTertiaryContainer = ColorMoss,

    background = ColorBone,
    onBackground = ColorInk,
    surface = ColorBone,
    onSurface = ColorInk,
    surfaceVariant = ColorSurface,
    onSurfaceVariant = ColorSlate,

    outline = ColorHair,
    outlineVariant = ColorSurfaceHi,

    error = ColorClay,
    onError = ColorBone,
    errorContainer = ColorSurface,
    onErrorContainer = ColorClay,
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
