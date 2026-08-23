package co.privado.finly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.privado.finly.ui.screens.allowedapps.AllowedAppsScreen
import co.privado.finly.ui.screens.biometric.BiometricLockScreen
import co.privado.finly.ui.screens.home.HomeScreen
import co.privado.finly.ui.screens.login.LoginScreen

@Composable
fun AppNavGraph(startDestination: String = Routes.Login) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = startDestination) {
        composable(Routes.Login) { LoginScreen(onLoginSuccess = { nav.navigate(Routes.Home) }) }
        composable(Routes.BiometricLock) { BiometricLockScreen(onUnlocked = { nav.navigate(Routes.Home) }) }
        composable(Routes.Home) { HomeScreen(nav) }
        composable(Routes.AllowedApps) { AllowedAppsScreen() }
        // TODO: Transactions, Accounts, Categories, ReviewQueue
    }
}
