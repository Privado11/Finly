package co.privado.finly

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import co.privado.finly.ui.navigation.AppNavGraph
import co.privado.finly.ui.theme.FinlyTheme
import dagger.hilt.android.AndroidEntryPoint

// FragmentActivity es obligatorio para BiometricPrompt (androidx.biometric)
@AndroidEntryPoint
@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinlyTheme {
                AppNavGraph()
            }
        }
    }
}
