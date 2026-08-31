package co.privado.finly.ui.navigation
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.privado.finly.R
import co.privado.finly.ui.screens.biometric.BiometricLockScreen
import co.privado.finly.ui.screens.login.LoginScreen
import co.privado.finly.ui.screens.register.RegisterScreen
import co.privado.finly.ui.theme.ColorInk
import co.privado.finly.ui.theme.ColorSlate
import co.privado.finly.ui.theme.IbmPlexMono

@androidx.compose.material3.ExperimentalMaterial3Api
@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
        is AuthState.Authenticated -> Routes.Main
        is AuthState.Unauthenticated -> Routes.Login
    }

    if (start == null) {
        // Splash — ícono oficial de la app mientras se verifica la sesión
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorInk),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = "Finly",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "FINLY",
                    style = TextStyle(
                        fontFamily = IbmPlexMono,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        letterSpacing = 0.14.sp,
                        color = ColorSlate
                    )
                )
            }
        }
        return
    }

    NavHost(
        navController = nav,
        startDestination = start,
        modifier = Modifier.fillMaxSize().background(ColorInk)
    ) {
        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    nav.navigate(Routes.Main) { popUpTo(Routes.Login) { inclusive = true } }
                },
                onNavigateToRegister = { nav.navigate(Routes.Register) }
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                onRegisterSuccess = {
                    nav.navigate(Routes.Main) { popUpTo(Routes.Login) { inclusive = true } }
                },
                onNavigateToLogin = { nav.popBackStack() }
            )
        }
        composable(Routes.BiometricLock) {
            val userName by authCheckViewModel.userName.collectAsState()

            BiometricLockScreen(
                userName = userName,
                onUnlocked = {
                    authCheckViewModel.onBiometricSuccess()
                    nav.navigate(Routes.Main) { popUpTo(Routes.BiometricLock) { inclusive = true } }
                },
                onUsePassword = {
                    authCheckViewModel.onLogout()
                    nav.navigate(Routes.Login) { popUpTo(Routes.BiometricLock) { inclusive = true } }
                }
            )
        }
        composable(Routes.Main) {
            MainScreen(
                onLogout = {
                    authCheckViewModel.onLogout()
                    nav.navigate(Routes.Login) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}
