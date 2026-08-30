package co.privado.finly.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import co.privado.finly.R

// ─────────────────────────────────────────────
//  Tipografía Finly (finly-design-system.html)
//
//  · Fraunces  → montos grandes, títulos de pantalla, balance card
//  · Inter     → UI general (body, labels, botones)
//  · IBM Plex Mono → cifras en listas de transacciones, tokens, fechas
// ─────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val FrauncesFont = GoogleFont("Fraunces")
val InterFont = GoogleFont("Inter")
val IbmPlexMonoFont = GoogleFont("IBM Plex Mono")

/** Serif — montos grandes, títulos principales */
val Fraunces = FontFamily(
    Font(googleFont = FrauncesFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = FrauncesFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = FrauncesFont, fontProvider = provider, weight = FontWeight.SemiBold),
)

/** Sans-serif — UI general */
val Inter = FontFamily(
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Bold),
)

/** Monoespaciada — cifras en listas, packageName de apps, fechas */
val IbmPlexMono = FontFamily(
    Font(googleFont = IbmPlexMonoFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = IbmPlexMonoFont, fontProvider = provider, weight = FontWeight.Medium),
)

// ─────────────────────────────────────────────
//  Escala tipográfica Material 3 mapeada al design system
// ─────────────────────────────────────────────
val Typography = Typography(
    // Balance card, totales grandes → Fraunces Medium ~38 sp
    displaySmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.25).sp
    ),
    // Títulos de sección grandes (pantalla Cuentas, encabezado Home) → Fraunces Medium ~26 sp
    headlineMedium = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    // Top app bar h2 → Fraunces
    titleLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    // Nombres de transacciones, cuentas → Inter SemiBold
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // Cuerpo general → Inter Regular
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Botones → Inter SemiBold
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Labels de campos, eyebrows (mayúsculas con letter-spacing) → IBM Plex Mono
    labelMedium = TextStyle(
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.sp
    ),
    // Cifras en listas de transacciones, fechas, packageName → IBM Plex Mono
    labelSmall = TextStyle(
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp
    ),
)

// ─── Alias semánticos (uso directo en Composables) ───────────────────────────
/** Monto grande de la balance card */
val TypographyAmountLarge = Typography.displaySmall

/** Monto en la lista de transacciones — IBM Plex Mono Medium */
val TypographyAmountList = TextStyle(
    fontFamily = IbmPlexMono,
    fontWeight = FontWeight.Medium,
    fontSize = 13.5.sp,
    lineHeight = 20.sp
)

/** Eyebrow / brand label ("FINLY", "BALANCE TOTAL") — IBM Plex Mono uppercase */
val TypographyEyebrow = TextStyle(
    fontFamily = IbmPlexMono,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.12.sp
)
