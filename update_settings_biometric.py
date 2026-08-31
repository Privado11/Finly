with open('app/src/main/java/co/privado/finly/ui/screens/settings/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Make sure LocalContext is imported or use fully qualified name
if "import androidx.compose.ui.platform.LocalContext" not in content:
    content = content.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.Alignment\nimport androidx.compose.ui.platform.LocalContext")

# 1. Insert LocalContext.current at the top of the composable
old_vars = """    val userEmail by viewModel.userEmail.collectAsState()
    val userName by viewModel.userName.collectAsState()
    
    var showEditNameDialog by remember { mutableStateOf(false) }"""

new_vars = """    val userEmail by viewModel.userEmail.collectAsState()
    val userName by viewModel.userName.collectAsState()
    
    var showEditNameDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current"""

content = content.replace(old_vars, new_vars)

# 2. Modify SettingsToggleRow for biometric
old_biometric = """            SettingsGroup {
                SettingsToggleRow(
                    icon = Icons.Filled.Fingerprint,
                    title = "Desbloqueo con huella",
                    checked = biometricEnabled,
                    onCheckedChange = { viewModel.setBiometricEnabled(it) }
                )
            }"""

new_biometric = """            SettingsGroup {
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
            }"""

content = content.replace(old_biometric, new_biometric)

with open('app/src/main/java/co/privado/finly/ui/screens/settings/SettingsScreen.kt', 'w') as f:
    f.write(content)
