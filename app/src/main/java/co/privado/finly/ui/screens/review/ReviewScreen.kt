package co.privado.finly.ui.screens.review

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReviewScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) { Text("Review", style = MaterialTheme.typography.headlineMedium) }
}
