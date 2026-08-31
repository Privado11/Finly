package co.privado.finly.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.ui.theme.*

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val notificationsEnabled by viewModel.appNotificationsEnabled.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userName by viewModel.userName.collectAsState()
    
    var showEditNameDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.background(ColorInk),
        containerColor = ColorInk
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)) {
                Text(
                    text = "CONFIGURACIÓN",
                    style = TypographyEyebrow,
                    color = ColorBrass,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Tus ajustes",
                    style = TextStyle(
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Medium,
                        fontSize = 26.sp,
                        color = ColorBone
                    )
                )
            }

            // Section: Cuenta
            Text(
                "Cuenta",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ColorBrass,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp)
            )
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.Person,
                    title = userName,
                    value = "Editar",
                    onClick = { showEditNameDialog = true }
                )
                HorizontalDivider(color = ColorHair, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Filled.Email,
                    title = "Correo",
                    value = userEmail,
                    onClick = { /* Read only */ }
                )
            }
            
            Spacer(Modifier.height(24.dp))

            // Section: Seguridad
            Text(
                "Seguridad",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ColorBrass,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp)
            )
            SettingsGroup {
                SettingsToggleRow(
                    icon = Icons.Filled.Fingerprint,
                    title = "Desbloqueo con huella",
                    checked = biometricEnabled,
                    onCheckedChange = { isEnabling -> 
                        if (isEnabling) {
                            val activity = context as? androidx.fragment.app.FragmentActivity
                            if (activity != null && co.privado.finly.util.BiometricHelper.puedeAutenticar(activity)) {
                                co.privado.finly.util.BiometricHelper.mostrarPrompt(
                                    activity = activity,
                                    onExito = { viewModel.setBiometricEnabled(true) },
                                    onFallo = { /* Falla, no hacemos nada y el toggle se queda desactivado */ }
                                )
                            } else {
                                android.widget.Toast.makeText(context, "Tu dispositivo no soporta o no tiene configurada la biometría", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.setBiometricEnabled(false)
                        }
                    }
                )
            }
            
            Spacer(Modifier.height(24.dp))

            // Section: Preferencias
            Text(
                "Preferencias",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ColorBrass,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 8.dp)
            )
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.DarkMode,
                    title = "Tema",
                    value = "Oscuro",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(color = ColorHair, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon = Icons.Filled.Notifications,
                    title = "Probar notificación",
                    value = "Enviar",
                    onClick = { viewModel.testNotification() }
                )
                HorizontalDivider(color = ColorHair, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsToggleRow(
                    icon = Icons.Filled.Notifications,
                    title = "Notificaciones",
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.setAppNotificationsEnabled(it) }
                )
            }
            
            Spacer(Modifier.height(24.dp))

            // Section: Logout
            SettingsGroup {
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Cerrar sesión",
                    titleColor = ColorClay,
                    iconColor = ColorClay,
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    }
                )
            }
        }
    }

    if (showEditNameDialog) {
        var firstName by remember { mutableStateOf(userName.split(" ").firstOrNull() ?: "") }
        var lastName by remember { mutableStateOf(userName.split(" ").drop(1).joinToString(" ")) }
        
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = ColorSurface,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Editar nombre", fontFamily = Fraunces, color = ColorBone) },
            text = {
                Column {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Nombres", fontFamily = Inter) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorBrass,
                            focusedLabelColor = ColorBrass,
                            unfocusedBorderColor = ColorHair,
                            unfocusedLabelColor = ColorSlate,
                            focusedTextColor = ColorBone,
                            unfocusedTextColor = ColorBone,
                            cursorColor = ColorBrass
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Apellidos", fontFamily = Inter) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorBrass,
                            focusedLabelColor = ColorBrass,
                            unfocusedBorderColor = ColorHair,
                            unfocusedLabelColor = ColorSlate,
                            focusedTextColor = ColorBone,
                            unfocusedTextColor = ColorBone,
                            cursorColor = ColorBrass
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateName(firstName.trim(), lastName.trim())
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorBrass, contentColor = ColorOnBrass),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar", fontFamily = Inter, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancelar", fontFamily = Inter, color = ColorSlate)
                }
            }
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ColorSurface,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = ColorBone,
    iconColor: androidx.compose.ui.graphics.Color = ColorSlate,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                fontFamily = Inter,
                fontSize = 14.sp,
                color = ColorSlate
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = ColorSlate, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = ColorBone,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ColorOnBrass,
                checkedTrackColor = ColorBrass,
                uncheckedThumbColor = ColorSlate,
                uncheckedTrackColor = ColorSurface,
                uncheckedBorderColor = ColorHair
            )
        )
    }
}
