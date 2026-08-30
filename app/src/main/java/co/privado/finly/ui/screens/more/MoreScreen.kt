package co.privado.finly.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.privado.finly.ui.theme.*

@Composable
fun MoreScreen(
    onNavigateToAccounts: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorInk)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "AJUSTES",
            style = TypographyEyebrow,
            color = ColorBrass,
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 4.dp)
        )
        Text("Más opciones", fontFamily = Fraunces, color = ColorBone, fontSize = 28.sp, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(4.dp))
        Text("Configuración y herramientas", fontFamily = Inter, color = ColorSlate, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Gestión",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ColorBrass,
                    modifier = Modifier.padding(horizontal = 4.dp).padding(bottom = 4.dp)
                )
            }
            item {
                MoreMenuItem(
                    icon = Icons.Filled.AccountBalanceWallet,
                    title = "Cuentas",
                    subtitle = "Administra tus cuentas bancarias y billeteras",
                    onClick = onNavigateToAccounts
                )
            }
            item {
                MoreMenuItem(
                    icon = Icons.Filled.Category,
                    title = "Categorías",
                    subtitle = "Personaliza íconos y colores de tus categorías",
                    onClick = onNavigateToCategories
                )
            }
            item {
                MoreMenuItem(
                    icon = Icons.Filled.RateReview,
                    title = "Bandeja de revisión",
                    subtitle = "Notificaciones que no se clasificaron automáticamente",
                    onClick = onNavigateToReview
                )
            }
            item {
                MoreMenuItem(
                    icon = Icons.Filled.Settings,
                    title = "Configuración",
                    subtitle = "Tema, biometría, permisos y cuenta",
                    onClick = onNavigateToSettings
                )
            }
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Próximamente",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ColorSlate,
                    modifier = Modifier.padding(horizontal = 4.dp).padding(bottom = 4.dp)
                )
            }
            item {
                MoreMenuItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Reportes",
                    subtitle = "Tendencias, exportar CSV/PDF",
                    onClick = {},
                    enabled = false
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.45f
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ColorSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (enabled) ColorBrass.copy(alpha = 0.15f) else ColorSlate.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, null,
                        tint = if (enabled) ColorBrass else ColorSlate,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = ColorBone.copy(alpha = alpha)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = ColorSlate.copy(alpha = alpha)
                )
            }
        }
    }
}
