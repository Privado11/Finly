package co.privado.finly.ui.components.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.privado.finly.ui.theme.ColorBone
import co.privado.finly.ui.theme.ColorBrass
import co.privado.finly.ui.theme.ColorOnBrass
import co.privado.finly.ui.theme.ColorClay
import co.privado.finly.ui.theme.ColorHair
import co.privado.finly.ui.theme.ColorInk
import co.privado.finly.ui.theme.ColorSlate
import co.privado.finly.ui.theme.ColorSurface
import co.privado.finly.ui.theme.ColorSurfaceHi
import co.privado.finly.ui.theme.Fraunces
import co.privado.finly.ui.theme.IbmPlexMono
import co.privado.finly.ui.theme.Inter
import co.privado.finly.ui.theme.TypographyEyebrow

// ─── Constantes de diseño ────────────────────────────────────────────────────
private val FieldShape = RoundedCornerShape(16.dp)
private val ButtonShape = RoundedCornerShape(16.dp)
private val AvatarShape = RoundedCornerShape(16.dp)

// ─────────────────────────────────────────────────────────────────────────────
//  Scaffold de pantalla auth
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AuthScreenLayout(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorInk)
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top
        ) { content() }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Encabezado — ícono oficial de la app + título Fraunces
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FinlyAuthHeader(title: String, subtitle: String) {
    // Ícono real de la app desde mipmap
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(co.privado.finly.R.mipmap.ic_launcher_foreground),
        contentDescription = "Finly",
        modifier = Modifier
            .size(56.dp)
            .clip(AvatarShape)
    )

    Spacer(Modifier.height(22.dp))

    // Título grande — Fraunces Medium (headlineMedium del sistema)
    Text(
        text = title,
        style = TextStyle(
            fontFamily = Fraunces,
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            color = ColorBone
        )
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = subtitle,
        style = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 13.5.sp,
            lineHeight = 20.sp,
            color = ColorSlate
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Campo de texto — estilo ledger (§ finly-design-system.html .field)
//  Fondo: ColorSurface, borde: ColorHair 1dp, sin outline M3 visible.
//  Label: IBM Plex Mono uppercase small arriba.
//  Valor: Inter 15sp ColorBone.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FinlyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    placeholder: String = ""
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(ColorSurface)
            .border(1.dp, ColorHair, FieldShape)
            .padding(horizontal = 17.dp, vertical = 13.dp)
    ) {
        // Label — IBM Plex Mono uppercase 10.5sp slate
        Text(
            text = label.uppercase(),
            style = TypographyEyebrow.copy(fontSize = 10.5.sp),
            color = ColorSlate,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = if (value.isEmpty()) ColorSlate.copy(alpha = 0.5f) else ColorBone
                ),
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction
                ),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontFamily = Inter,
                                    fontSize = 15.sp,
                                    color = Color(0xFF4C555F)
                                )
                            )
                        }
                        inner()
                    }
                },
                modifier = Modifier.weight(1f)
            )
            trailingIcon?.invoke()
        }
    }
}

@Composable
fun EmailField(value: String, onValueChange: (String) -> Unit, enabled: Boolean) =
    FinlyField(
        label = "Correo electrónico",
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        keyboardType = KeyboardType.Email,
        placeholder = "tucorreo@ejemplo.com"
    )

@Composable
fun NameField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier
) = FinlyField(
    label = label,
    value = value,
    onValueChange = onValueChange,
    enabled = enabled,
    modifier = modifier
)

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    imeAction: ImeAction = ImeAction.Done
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    FinlyField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        placeholder = "••••••••",
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            // Box en vez de IconButton para no heredar el mínimo de 48dp de M3
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clickable { visible = !visible },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = ColorSlate,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Botón primario — brass sólido (§ .btn-primary)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PrimaryAuthButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(ButtonShape)
            .background(if (loading) ColorBrass.copy(alpha = 0.5f) else ColorBrass)
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = ColorOnBrass,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    color = ColorOnBrass
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Botón Google — borde ColorHair, sin relleno (§ .btn-google)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GoogleAuthButton(loading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(ButtonShape)
            .border(1.dp, ColorHair, ButtonShape)
            .background(Color.Transparent)
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // "G" monoespaciado como en el HTML
            Text(
                text = "G",
                style = TextStyle(
                    fontFamily = IbmPlexMono,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = ColorBone
                )
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Continuar con Google",
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = ColorBone
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Divider "o" — línea + texto slate
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AuthDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = ColorHair)
        Text(
            text = "  o  ",
            style = TextStyle(fontFamily = IbmPlexMono, fontSize = 11.sp, color = ColorSlate)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = ColorHair)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Error inline — surface elevada, texto clay
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AuthError(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ColorSurfaceHi)
            .border(1.dp, ColorClay.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = message,
            style = TextStyle(
                fontFamily = Inter,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = ColorClay
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Pie de pantalla — "¿Ya tienes cuenta? Iniciar sesión"
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AuthFooter(question: String, action: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = question,
            style = TextStyle(fontFamily = Inter, fontSize = 12.5.sp, color = ColorSlate)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = action,
            style = TextStyle(
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp,
                color = ColorBrass
            ),
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp)
        )
    }
}
