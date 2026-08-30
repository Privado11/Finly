package co.privado.finly.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {
    fun puedeAutenticar(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        val canAuth = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS || canAuth == BiometricManager.BIOMETRIC_STATUS_UNKNOWN
    }

    fun mostrarPrompt(
        activity: FragmentActivity,
        onExito: () -> Unit,
        onFallo: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onExito() }
            override fun onAuthenticationFailed() { onFallo() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { onFallo() }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear Finly")
            .setSubtitle("Usa tu huella o PIN para continuar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(info)
    }
}
