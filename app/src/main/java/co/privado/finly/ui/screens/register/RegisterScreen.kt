package co.privado.finly.ui.screens.register

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
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
import co.privado.finly.ui.components.auth.NameField
import co.privado.finly.ui.components.auth.PasswordField
import co.privado.finly.ui.components.auth.PrimaryAuthButton

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
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
            title = "Crea tu cuenta",
            subtitle = "Empieza a tomar decisiones más claras sobre tu dinero."
        )
        Spacer(Modifier.height(28.dp))
        NameField(uiState.firstName, viewModel::onFirstNameChange, "Nombre", !uiState.isLoading, Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        NameField(uiState.lastName, viewModel::onLastNameChange, "Apellido", !uiState.isLoading, Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        EmailField(uiState.email, viewModel::onEmailChange, enabled = !uiState.isLoading)
        Spacer(Modifier.height(12.dp))
        PasswordField(uiState.password, viewModel::onPasswordChange, "Crea una contraseña", !uiState.isLoading)
        Spacer(Modifier.height(12.dp))
        PasswordField(uiState.confirmPassword, viewModel::onConfirmPasswordChange, "Confirma tu contraseña", !uiState.isLoading, ImeAction.Done)
        uiState.error?.let {
            Spacer(Modifier.height(16.dp))
            AuthError(it)
        }
        Spacer(Modifier.height(24.dp))
        PrimaryAuthButton("Crear mi cuenta", uiState.isLoading) { viewModel.register(onRegisterSuccess) }
        Spacer(Modifier.height(24.dp))
        AuthDivider()
        Spacer(Modifier.height(24.dp))
        GoogleAuthButton(uiState.isLoading) {
            activity?.let { viewModel.registerWithGoogle(it, onRegisterSuccess) }
        }
        Spacer(Modifier.height(24.dp))
        AuthFooter("¿Ya tienes una cuenta?", "Iniciar sesión", onNavigateToLogin)
    }
}
