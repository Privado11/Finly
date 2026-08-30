package co.privado.finly.ui.screens.allowedapps

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import co.privado.finly.ui.theme.*

@Composable
fun AllowedAppsScreen(viewModel: AllowedAppsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var query by rememberSaveable { mutableStateOf("") }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermission() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    val apps = state.apps.filter { it.label.contains(query, true) || it.packageName.contains(query, true) }.sortedByDescending { it.packageName in state.allowedPackages }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorInk)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "APPS",
            style = TypographyEyebrow,
            color = ColorBrass,
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 4.dp)
        )
        Text("Apps monitoreadas", fontFamily = Fraunces, color = ColorBone, fontSize = 28.sp, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(4.dp))
        Text("Tú decides qué notificaciones se procesan", fontFamily = Inter, color = ColorSlate, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(24.dp))
        
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ColorBrass) }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { 
                    PermissionCard(state.listenerEnabled) { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) } 
                }
                
                item { 
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ColorSurface, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = ColorSlate, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text("Buscar una app...", color = ColorSlate, fontFamily = Inter, fontSize = 15.sp)
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                textStyle = TextStyle(color = ColorBone, fontFamily = Inter, fontSize = 15.sp),
                                cursorBrush = SolidColor(ColorBrass),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                item { 
                    Text(
                        text = "Solo activa apps bancarias o billeteras que quieras registrar.", 
                        fontFamily = Inter, 
                        color = ColorSlate, 
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) 
                }
                
                items(apps, key = { it.packageName }) { app -> 
                    AppRow(app, app.packageName in state.allowedPackages, app.packageName in state.isUpdating, state.listenerEnabled) { viewModel.toggle(app, it) } 
                }
                
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
    
    state.error?.let { 
        AlertDialog(
            onDismissRequest = viewModel::dismissError, 
            title = { Text("No pudimos guardar el cambio", fontFamily = Fraunces, color = ColorBone) }, 
            text = { Text(it, color = ColorSlate, fontFamily = Inter) }, 
            confirmButton = { 
                TextButton(onClick = viewModel::dismissError) { Text("Entendido", color = ColorBrass) } 
            },
            containerColor = ColorSurface
        ) 
    }
}

@Composable 
private fun PermissionCard(enabled: Boolean, openSettings: () -> Unit) {
    val bgColor = if (enabled) ColorMoss.copy(alpha = 0.15f) else ColorError.copy(alpha = 0.15f)
    val contentColor = if (enabled) ColorMoss else ColorError
    val icon = if (enabled) Icons.Filled.NotificationsActive else Icons.Rounded.NotificationsOff
    val title = if (enabled) "Acceso activo" else "Acceso inactivo"
    val subtitle = if (enabled) "Finly leerá las apps seleccionadas." else "Requiere permiso en Ajustes."
    
    Surface(shape = RoundedCornerShape(20.dp), color = bgColor) { 
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) { 
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) { 
                Text(title, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = contentColor)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontFamily = Inter, fontSize = 13.sp, color = contentColor.copy(alpha = 0.8f)) 
            }
            TextButton(
                onClick = openSettings,
                colors = ButtonDefaults.textButtonColors(contentColor = contentColor)
            ) { 
                Text(if (enabled) "Revisar" else "Activar", fontFamily = Inter, fontWeight = FontWeight.Bold) 
            } 
        } 
    }
}

@Composable 
private fun AppRow(app: InstalledApp, checked: Boolean, updating: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = ColorSurface, modifier = Modifier.alpha(if (enabled) 1f else 0.5f)) { 
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) { 
            AsyncImage(app.icon, contentDescription = null, modifier = Modifier.size(44.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) { 
                Text(app.label, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = ColorBone)
                Spacer(Modifier.height(2.dp))
                Text(app.packageName, fontFamily = Inter, fontSize = 12.sp, color = ColorSlate, maxLines = 1) 
            }
            if (updating) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = ColorBrass)
            } else {
                Switch(
                    checked = checked, 
                    onCheckedChange = onCheckedChange,
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ColorOnBrass,
                        checkedTrackColor = ColorBrass,
                        uncheckedThumbColor = ColorSlate,
                        uncheckedTrackColor = ColorInk,
                        uncheckedBorderColor = ColorHair
                    )
                ) 
            }
        } 
    }
}
