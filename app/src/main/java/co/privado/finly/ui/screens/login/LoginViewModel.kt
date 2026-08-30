package co.privado.finly.ui.screens.login

import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity
import co.privado.finly.BuildConfig
import co.privado.finly.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v.trim(), error = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, error = null) }

    private fun validar(): String? {
        val s = _uiState.value
        if (s.email.isBlank() || !s.email.contains("@")) return "Ingresa un email válido"
        if (s.password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        return null
    }

    fun login(onSuccess: () -> Unit) {
        val msg = validar()
        if (msg != null) { _uiState.update { it.copy(error = msg) }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = authRepository.login(_uiState.value.email, _uiState.value.password)
            if (res.isSuccess) onSuccess()
            else _uiState.update { it.copy(error = mapError(res.exceptionOrNull())) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun loginWithGoogle(activity: FragmentActivity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
                    _uiState.update { it.copy(error = "Google Sign-In no configurado (falta Web Client ID)") }
                    return@launch
                }

                val idToken = obtenerGoogleIdToken(activity)
                if (idToken == null) {
                    // Usuario canceló — no mostrar error
                    return@launch
                }

                val res = authRepository.signInWithGoogle(idToken)
                if (res.isSuccess) onSuccess()
                else _uiState.update { it.copy(error = mapError(res.exceptionOrNull())) }

            } catch (e: Exception) {
                val msg = e.message ?: ""
                when {
                    // Cancelación explícita del usuario — silencio
                    msg.contains("cancel", ignoreCase = true) -> { /* no-op */ }
                    // Error de configuración (SHA-1 o Client ID)
                    msg.contains("10:", ignoreCase = true) ||
                    msg.contains("ApiException", ignoreCase = true) ->
                        _uiState.update { it.copy(error = "Error de configuración de Google (código 10). Verifica el SHA-1 en Google Cloud Console.") }
                    else -> _uiState.update { it.copy(error = mapError(e)) }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Muestra el selector de cuentas Google y retorna el ID token.
     * Retorna null si el usuario cancela. Lanza excepción en error real.
     *
     * Va directo al selector sin intentar sign-in silencioso — el silent flow
     * con setAutoSelectEnabled(true) puede colgar indefinidamente esperando GMS.
     */
    private suspend fun obtenerGoogleIdToken(activity: FragmentActivity): String? {
        val credentialManager = CredentialManager.create(activity)
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // siempre muestra el selector
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)           // nunca silencioso
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        return try {
            val result = credentialManager.getCredential(activity, request)
            GoogleIdTokenCredential.createFrom(result.credential.data).idToken
        } catch (e: GetCredentialException) {
            if (e.message.orEmpty().contains("cancel", ignoreCase = true)) null
            else throw e
        }
    }

    private fun mapError(t: Throwable?): String {
        val raw = t?.message ?: "Error desconocido"
        return when {
            raw.contains("Invalid login", true) || raw.contains("invalid_credentials", true) -> "Email o contraseña incorrectos"
            raw.contains("Email not confirmed", true) -> "Confirma tu email antes de ingresar"
            raw.contains("network", true) || raw.contains("Unable to resolve", true) -> "Sin conexión a internet"
            else -> raw.take(180)
        }
    }
}
