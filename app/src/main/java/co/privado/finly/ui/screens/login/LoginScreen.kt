package co.privado.finly.ui.screens.login

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.ui.components.auth.AuthDivider
import co.privado.finly.ui.components.auth.AuthError
import co.privado.finly.ui.components.auth.AuthFooter
import co.privado.finly.ui.components.auth.AuthScreenLayout
import co.privado.finly.ui.components.auth.EmailField
import co.privado.finly.ui.components.auth.FinlyAuthHeader
import co.privado.finly.ui.components.auth.GoogleAuthButton
import co.privado.finly.ui.components.auth.PasswordField
import co.privado.finly.ui.components.auth.PrimaryAuthButton

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var activity = context as? FragmentActivity
    if (activity == null && context is android.content.ContextWrapper) {
        var base = context.baseContext
        while (base is android.content.ContextWrapper && base !is FragmentActivity) {
            base = base.baseContext
        }
        activity = base as? FragmentActivity
    }

    AuthScreenLayout {
        FinlyAuthHeader(
            title = "Qué bueno verte",
            subtitle = "Ingresa para tener tus finanzas bajo control."
        )
        Spacer(Modifier.height(32.dp))
        EmailField(uiState.email, viewModel::onEmailChange, enabled = !uiState.isLoading)
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Contraseña",
            enabled = !uiState.isLoading,
            imeAction = ImeAction.Done
        )
        uiState.error?.let {
            Spacer(Modifier.height(16.dp))
            AuthError(it)
        }
        Spacer(Modifier.height(24.dp))
        PrimaryAuthButton("Ingresar", uiState.isLoading) { viewModel.login(onLoginSuccess) }
        Spacer(Modifier.height(24.dp))
        AuthDivider()
        Spacer(Modifier.height(24.dp))
        GoogleAuthButton(uiState.isLoading) {
            activity?.let { viewModel.loginWithGoogle(it, onLoginSuccess) }
        }
        Spacer(Modifier.height(24.dp))
        AuthFooter("¿Aún no tienes cuenta?", "Crear cuenta", onNavigateToRegister)
    }
}
