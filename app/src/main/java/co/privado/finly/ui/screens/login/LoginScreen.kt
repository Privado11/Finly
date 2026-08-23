package co.privado.finly.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        Text("Finly — Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = uiState.email, onValueChange = viewModel::onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = uiState.password, onValueChange = viewModel::onPasswordChange, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Button(onClick = { viewModel.login(onLoginSuccess) }, modifier = Modifier.fillMaxWidth()) { Text("Ingresar") }
        OutlinedButton(onClick = { viewModel.loginWithGoogle(onLoginSuccess) }, modifier = Modifier.fillMaxWidth()) { Text("Continuar con Google") }
        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
