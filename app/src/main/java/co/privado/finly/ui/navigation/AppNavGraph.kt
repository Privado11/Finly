package co.privado.finly.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.privado.finly.ui.screens.allowedapps.AllowedAppsScreen
import co.privado.finly.ui.screens.accounts.AccountsScreen
import co.privado.finly.ui.screens.categories.CategoriesScreen
import co.privado.finly.ui.screens.transactions.TransactionsScreen
import co.privado.finly.ui.screens.biometric.BiometricLockScreen
import co.privado.finly.ui.screens.home.HomeScreen
import co.privado.finly.ui.screens.login.LoginScreen
import co.privado.finly.ui.screens.register.RegisterScreen

@Composable
fun AppNavGraph(
    authCheckViewModel: AuthCheckViewModel = hiltViewModel()
) {
    val nav = rememberNavController()
    val authState by authCheckViewModel.state.collectAsState()

    // §3.1 Frontend — diagrama de estados: decide start por sesión guardada
    val start = when (authState) {
        is AuthState.Loading -> null // muestra splash
        is AuthState.NeedsBiometric -> Routes.BiometricLock
        is AuthState.Authenticated -> Routes.Home
        is AuthState.Unauthenticated -> Routes.Login
    }

    if (start == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    // Si después de login hay biometría disponible, la próxima apertura pedirá huella;
                    // por ahora va directo a Home
                    nav.navigate(Routes.Home) { popUpTo(Routes.Login) { inclusive = true } }
                },
                onNavigateToRegister = { nav.navigate(Routes.Register) }
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                onRegisterSuccess = {
                    nav.navigate(Routes.Home) { popUpTo(Routes.Login) { inclusive = true } }
                },
                onNavigateToLogin = { nav.popBackStack() }
            )
        }
        composable(Routes.BiometricLock) {
            BiometricLockScreen(
                onUnlocked = {
                    authCheckViewModel.onBiometricSuccess()
                    nav.navigate(Routes.Home) { popUpTo(Routes.BiometricLock) { inclusive = true } }
                },
                onUsePassword = {
                    // Fallback §3.1/§3.5: si falla huella, opción "usar contraseña"
                    nav.navigate(Routes.Login) { popUpTo(Routes.BiometricLock) { inclusive = true } }
                }
            )
        }
        composable(Routes.Home) { HomeScreen(nav) }
        composable(Routes.Accounts) { AccountsScreen() }
        composable(Routes.Categories) { CategoriesScreen() }
        composable(Routes.Transactions) { TransactionsScreen(onSaved = { nav.popBackStack() }) }
        composable(Routes.AllowedApps) { AllowedAppsScreen() }
        // TODO: ReviewQueue (roadmap §9)
    }
}
