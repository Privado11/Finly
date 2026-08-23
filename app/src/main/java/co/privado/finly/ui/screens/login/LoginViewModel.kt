package co.privado.finly.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v) }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = authRepository.login(_uiState.value.email, _uiState.value.password)
            if (res.isSuccess) onSuccess() else _uiState.update { it.copy(error = res.exceptionOrNull()?.message) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun loginWithGoogle(onSuccess: () -> Unit) {
        // El flujo Credential Manager se inicia desde la Activity; aquí solo se delega el idToken
        // Ver docs/frontend 3.3 — se implementa en la siguiente iteración con Activity result.
    }
}
