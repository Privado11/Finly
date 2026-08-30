package co.privado.finly.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.data.local.SessionDataStore
import co.privado.finly.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    
    val userName: StateFlow<String> = sessionStore.userDisplayState
        .map { it.first }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Usuario")

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val valid = try {
                // isSessionValid confía en expires_at local; solo hace red si el token expiró (refresh).
                authRepository.isSessionValid()
            } catch (_: Exception) {
                // Fallback offline: si el token local todavía no expiró según expires_at guardado,
                // dejamos pasar — la sesión de Supabase se restaurará cuando haya red.
                // Solo llegamos aquí si isSessionValid lanzó excepción (ej. red caída durante refresh).
                sessionStore.isSessionValid()
            }
            if (valid) {
                val needsBio = sessionStore.isBiometricEnabled()
                _state.value = if (needsBio) AuthState.NeedsBiometric else AuthState.Authenticated
            } else {
                _state.value = AuthState.Unauthenticated
            }
        }
    }

    fun onBiometricSuccess() { _state.value = AuthState.Authenticated }
    fun onLogout() { _state.value = AuthState.Unauthenticated }
    }

