package co.privado.finly.ui.screens.transaction_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.privado.finly.domain.model.Transaction
import co.privado.finly.domain.model.TransactionSource
import co.privado.finly.domain.model.TransactionType
import co.privado.finly.ui.theme.*
import co.privado.finly.util.toIcon
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.runtime.LaunchedEffect

@Composable
fun TransactionDetailScreen(onBack: () -> Unit, onEdit: (String) -> Unit = {}, viewModel: TransactionDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorInk)
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorBrass)
            }
        } else if (state.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.error!!, color = ColorClay)
            }
        } else {
            val tx = state.transaction
            if (tx != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    // Topbar
                    var showDeleteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = ColorBone
                            )
                        }
                        Text(
                            text = "Detalle",
                            style = TextStyle(
                                fontFamily = Fraunces,
                                fontWeight = FontWeight.Medium,
                                fontSize = 22.sp,
                                color = ColorBone
                            )
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { state.transaction?.id?.let { onEdit(it) } }, modifier = Modifier.size(36.dp)) {
                            Icon(androidx.compose.material.icons.Icons.Filled.Edit, "Editar", tint = ColorBone)
                        }
                        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(36.dp)) {
                            Icon(androidx.compose.material.icons.Icons.Filled.Delete, "Eliminar", tint = ColorError)
                        }
                    }

                    // Card 1: Monto y Tipo
                    DetailCard {
                        val isInc = tx.type == TransactionType.income
                        val isExp = tx.type == TransactionType.expense
                        val prefix = if (isInc) "+" else if (isExp) "−" else ""
                        val amountColor = if (isInc) ColorMoss else ColorBone
                        val iconBg = if (isInc) ColorMoss.copy(alpha = 0.16f) else if (isExp) ColorClay.copy(alpha = 0.16f) else ColorSlate.copy(alpha = 0.16f)
                        val iconText = if (isInc) "↑" else if (isExp) "↓" else "↔"
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (state.categoryIcon.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color(0xFF162B28)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = state.categoryIcon.toIcon(),
                                        contentDescription = null,
                                        tint = ColorMoss,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(iconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = iconText, color = amountColor, fontSize = 20.sp)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = tx.type.label(),
                                    style = TypographyEyebrow,
                                    color = ColorSlate,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "$prefix${formatMoney(tx.amount)} ${tx.currency}",
                                    style = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 28.sp, color = amountColor)
                                )
                            }
                        }
                    }

                    // Card 2: Detalles
                    DetailCard {
                        DetailRow("Fecha", formatDate(tx.date))
                        Divider()
                        
                        val defaultTitle = "Movimiento"
                        val displayTitle = tx.merchant?.takeIf { it.isNotBlank() } ?: (tx.categoryId?.let { state.categoryName } ?: defaultTitle)
                        DetailRow(if (tx.type == TransactionType.expense) "Comercio" else "Descripción", displayTitle)
                        Divider()
                        
                        DetailRow("Cuenta", state.sourceAccountName)
                        Divider()
                        DetailRow("Categoría", state.categoryName)
                        
                        Divider()
                        DetailRow("Origen de datos", tx.source.label())
                    }

                    // Card 3: Raw notification (if present)
                    if (!tx.rawNotification.isNullOrBlank()) {
                        DetailCard {
                            Text(
                                text = "NOTIFICACIÓN ORIGINAL",
                                style = TypographyEyebrow,
                                color = ColorBrass,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ColorSurfaceHi)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = tx.rawNotification,
                                    style = TextStyle(fontFamily = IbmPlexMono, fontSize = 12.5.sp, color = ColorBone, lineHeight = 18.sp)
                                )
                            }
                        }
                    }
                    

                    
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Eliminar movimiento", fontFamily = Fraunces, color = ColorBone) },
                            text = { Text("¿Estás seguro de que deseas eliminar este movimiento? Esta acción no se puede deshacer.", fontFamily = Inter, color = ColorSlate) },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showDeleteDialog = false
                                        viewModel.deleteTransaction()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorError, contentColor = ColorBone)
                                ) {
                                    Text("Eliminar", fontFamily = Inter, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancelar", fontFamily = Inter, color = ColorSlate)
                                }
                            },
                            containerColor = ColorSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(ColorSurface)
            .border(1.dp, ColorHair, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(fontSize = 14.sp, color = ColorSlate)
        )
        Text(
            text = value,
            style = TextStyle(fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = ColorBone)
        )
    }
}

@Composable
private fun Divider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ColorHair))
}

private fun TransactionType.label() = when (this) { 
    TransactionType.income -> "Ingreso"
    TransactionType.expense -> "Gasto"
}

private fun TransactionSource.label() = when (this) {
    TransactionSource.manual -> "Registro manual"
    TransactionSource.notification_regex -> "Auto (Reglas)"
    TransactionSource.notification_llm -> "Auto (IA)"
}

private fun formatMoney(value: Double): String = "$" + NumberFormat.getNumberInstance(
    Locale.Builder().setLanguage("es").setRegion("CO").build()
).apply { maximumFractionDigits = 0 }.format(value)

private fun formatDate(isoDate: String): String {
    return runCatching {
        val instant = Instant.parse(isoDate)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale("es", "ES"))
        zonedDateTime.format(formatter).replaceFirstChar { it.uppercase() }
    }.getOrDefault(isoDate)
}
