package co.privado.finly.ui.screens.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.domain.model.ReviewQueueItem
import co.privado.finly.ui.theme.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.Duration

@Composable
fun ReviewScreen(viewModel: ReviewViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    var itemToDelete by remember { mutableStateOf<ReviewQueueItem?>(null) }
    var itemToClassify by remember { mutableStateOf<ReviewQueueItem?>(null) }
    
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.background(ColorInk),
        containerColor = ColorInk
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 4.dp, top = 24.dp)) {
                Text("BANDEJA DE REVISIÓN", style = TypographyEyebrow, color = ColorBrass)
                Spacer(Modifier.height(4.dp))
                if (uiState.isLoading) {
                    Text("Cargando...", fontFamily = Inter, fontSize = 14.sp, color = ColorSlate)
                } else if (uiState.pendingItems.isEmpty()) {
                    Text("Todo al día", fontFamily = Inter, fontSize = 14.sp, color = ColorSlate)
                } else {
                    Text("${uiState.pendingItems.size} movimientos sin clasificar", fontFamily = Inter, fontSize = 14.sp, color = ColorSlate)
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    CircularProgressIndicator(color = ColorBrass) 
                }
            } else if (uiState.pendingItems.isEmpty()) {
                EmptyReviewState(Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.pendingItems, key = { it.id ?: "" }) { item ->
                        ReviewCard(
                            item = item,
                            onDiscard = { itemToDelete = item },
                            onClassify = { itemToClassify = item }
                        )
                    }
                }
            }
        }
    }

    // Modal para Descartar
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Descartar notificación", fontFamily = Fraunces, color = ColorBone) },
            text = { Text("¿Estás seguro de que deseas descartar esta notificación? Esta acción no se puede deshacer.", fontFamily = Inter, color = ColorSlate) },
            confirmButton = {
                Button(
                    onClick = {
                        item.id?.let { id -> viewModel.markAsResolved(id, "Notificación descartada") }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorError, contentColor = ColorBone)
                ) {
                    Text("Descartar", fontFamily = Inter, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar", fontFamily = Inter, color = ColorSlate)
                }
            },
            containerColor = ColorSurface
        )
    }

    // Modal de Clasificar (falso/visual por ahora, pero usando el ID real al guardar)
    itemToClassify?.let { item ->
        ClassifyDialog(
            item = item,
            accounts = uiState.accounts,
            categories = uiState.categories,
            onDismiss = { itemToClassify = null },
            onSave = { amount, accountId, categoryId, type, merchant ->
                item.id?.let { id -> 
                    viewModel.saveTransactionAndResolve(id, amount, accountId, categoryId, type, merchant, merchant) 
                }
                itemToClassify = null
            }
        )
    }
    
    uiState.error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("Ups...", fontFamily = Fraunces, color = ColorBone) },
            text = { Text(message, fontFamily = Inter, color = ColorSlate) },
            confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("Entendido", color = ColorBrass) } },
            containerColor = ColorSurface, titleContentColor = ColorBone, textContentColor = ColorSlate
        )
    }
}

@Composable
private fun EmptyReviewState(modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(shape = RoundedCornerShape(28.dp), color = ColorSurface, modifier = Modifier.size(96.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(44.dp), tint = ColorBrass)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Todo clasificado", fontFamily = Fraunces, fontSize = 24.sp, color = ColorBone)
        Spacer(Modifier.height(8.dp))
        Text("Cuando una notificación no se entienda del todo, aparecerá aquí para que la revises.", fontFamily = Inter, fontSize = 15.sp, color = ColorSlate, modifier = Modifier.padding(horizontal = 36.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun ReviewCard(item: ReviewQueueItem, onDiscard: () -> Unit, onClassify: () -> Unit) {
    val timeAgo = remember(item.createdAt) { getTimeAgo(item.createdAt) }
    
    Surface(shape = RoundedCornerShape(20.dp), color = ColorSurface) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, modifier = Modifier.size(16.dp), tint = ColorClay)
                    Spacer(Modifier.width(6.dp))
                    Text(item.packageName.replace("com.", "").capitalize(), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ColorBone)
                }
                Text(timeAgo, fontFamily = Inter, fontSize = 12.sp, color = ColorSlate)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "\"${item.originalText}\"",
                fontFamily = Inter,
                fontSize = 15.sp,
                color = ColorBone,
                fontStyle = FontStyle.Italic,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDiscard) {
                    Text("Descartar", fontFamily = Inter, color = ColorSlate)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onClassify,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorBrass, contentColor = ColorOnBrass),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clasificar", fontFamily = Inter, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getTimeAgo(createdAt: String?): String {
    if (createdAt == null) return "hace poco"
    return try {
        val instant = Instant.parse(createdAt)
        val now = Instant.now()
        val minutes = Duration.between(instant, now).toMinutes()
        when {
            minutes < 1 -> "ahora mismo"
            minutes < 60 -> "hace $minutes min"
            minutes < 1440 -> "hace ${minutes / 60} h"
            else -> "hace ${minutes / 1440} d"
        }
    } catch (e: Exception) {
        "hace poco"
    }
}
