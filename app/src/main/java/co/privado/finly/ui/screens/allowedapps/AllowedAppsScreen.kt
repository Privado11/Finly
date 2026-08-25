package co.privado.finly.ui.screens.allowedapps

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllowedAppsScreen(viewModel: AllowedAppsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var query by rememberSaveable { mutableStateOf("") }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermission() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val apps = state.apps.filter { it.label.contains(query, true) || it.packageName.contains(query, true) }
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = { TopAppBar(title = { Column { Text("Apps monitoreadas", style = MaterialTheme.typography.titleLarge); Text("Tú decides qué notificaciones se procesan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }) }
    ) { padding ->
        if (state.isLoading) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { PermissionCard(state.listenerEnabled) { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) } }
            item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Buscar una app") }, singleLine = true, leadingIcon = { Icon(Icons.Filled.Search, null) }) }
            item { Text("Solo activa apps bancarias o billeteras que quieras registrar.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(apps, key = { it.packageName }) { app -> AppRow(app, app.packageName in state.allowedPackages, app.packageName in state.isUpdating) { viewModel.toggle(app, it) } }
        }
    }
    state.error?.let { AlertDialog(onDismissRequest = viewModel::dismissError, title = { Text("No pudimos guardar el cambio") }, text = { Text(it) }, confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("Entendido") } }) }
}

@Composable private fun PermissionCard(enabled: Boolean, openSettings: () -> Unit) {
    val color = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val onColor = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    Surface(shape = RoundedCornerShape(20.dp), color = color) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.NotificationsActive, null, tint = onColor); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(if (enabled) "Acceso a notificaciones activo" else "Activa el acceso a notificaciones", style = MaterialTheme.typography.labelLarge, color = onColor); Text(if (enabled) "Finly solo procesará las apps que actives abajo." else "Android requiere que lo habilites en Ajustes.", style = MaterialTheme.typography.bodySmall, color = onColor) }; TextButton(onClick = openSettings) { Text(if (enabled) "Revisar" else "Activar") } } }
}

@Composable private fun AppRow(app: InstalledApp, checked: Boolean, updating: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { AsyncImage(app.icon, null, Modifier.size(42.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(app.label, style = MaterialTheme.typography.bodyLarge); Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }; if (updating) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) else Switch(checked, onCheckedChange) } }
}
