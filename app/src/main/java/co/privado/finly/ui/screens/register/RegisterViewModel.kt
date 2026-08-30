package co.privado.finly.ui.screens.register

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

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onFirstNameChange(v: String) = _uiState.update { it.copy(firstName = v, error = null) }
    fun onLastNameChange(v: String) = _uiState.update { it.copy(lastName = v, error = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v.trim(), error = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, error = null) }

    private fun validar(): String? {
        val s = _uiState.value
        if (s.firstName.isBlank()) return "Ingresa tu nombre"
        if (s.lastName.isBlank()) return "Ingresa tu apellido"
        if (s.email.isBlank() || !s.email.contains("@")) return "Ingresa un email válido"
        if (s.password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        if (s.password != s.confirmPassword) return "Las contraseñas no coinciden"
        return null
    }

    fun register(onSuccess: () -> Unit) {
        val msg = validar()
        if (msg != null) { _uiState.update { it.copy(error = msg) }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val s = _uiState.value
            val res = authRepository.signUp(s.email, s.password, s.firstName.trim(), s.lastName.trim())
            if (res.isSuccess) onSuccess()
            else _uiState.update { it.copy(error = mapError(res.exceptionOrNull())) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun registerWithGoogle(activity: FragmentActivity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
                    _uiState.update { it.copy(error = "Google Sign-In no configurado (falta Web Client ID)") }
                    return@launch
                }
                val idToken = obtenerGoogleIdToken(activity) ?: return@launch // cancelado
                val res = authRepository.signInWithGoogle(idToken)
                if (res.isSuccess) onSuccess()
                else _uiState.update { it.copy(error = mapError(res.exceptionOrNull())) }
            } catch (e: Exception) {
                val msg = e.message ?: ""
                when {
                    msg.contains("cancel", ignoreCase = true) -> { /* silencio */ }
                    msg.contains("10:", ignoreCase = true) || msg.contains("ApiException", ignoreCase = true) ->
                        _uiState.update { it.copy(error = "Error de configuración de Google (código 10). Verifica el SHA-1 en Google Cloud Console.") }
                    else -> _uiState.update { it.copy(error = mapError(e)) }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun obtenerGoogleIdToken(activity: FragmentActivity): String? {
        val credentialManager = CredentialManager.create(activity)
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // siempre muestra el selector
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)           // nunca silencioso
            .build()
        return try {
            val result = credentialManager.getCredential(
                activity,
                GetCredentialRequest.Builder().addCredentialOption(option).build()
            )
            GoogleIdTokenCredential.createFrom(result.credential.data).idToken
        } catch (e: GetCredentialException) {
            if (e.message.orEmpty().contains("cancel", ignoreCase = true)) null else throw e
        }
    }

    private fun mapError(t: Throwable?): String {
        val raw = t?.message ?: "Error desconocido"
        return when {
            raw.contains("already registered", true) || raw.contains("User already registered", true) -> "Ese email ya está registrado"
            raw.contains("network", true) -> "Sin conexión a internet"
            else -> raw.take(180)
        }
    }
}
