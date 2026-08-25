package co.privado.finly.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.data.local.SessionDataStore
import co.privado.finly.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data object NeedsBiometric : AuthState()
    data object Authenticated : AuthState()
}

@HiltViewModel
class AuthCheckViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionDataStore
) : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            try {
                // Valida JWT guardado + sesión de Supabase (refresh implícito si hay refreshToken válido)
                val hasLocalToken = sessionStore.getAccessToken() != null
                val supabaseValid = authRepository.isSessionValid()
                // Si hay token local y Supabase lo reconoce, consideramos sesión válida
                val valid = hasLocalToken && supabaseValid
                _state.value = if (valid) AuthState.NeedsBiometric else AuthState.Unauthenticated
            } catch (_: Exception) {
                _state.value = AuthState.Unauthenticated
            }
        }
    }

    fun onBiometricSuccess() { _state.value = AuthState.Authenticated }
    fun onLogout() { _state.value = AuthState.Unauthenticated }
}
