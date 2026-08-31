import re

with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'r') as f:
    content = f.read()

# Add edit and delete icons to Topbar
old_topbar = """                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
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
                        Column {
                            Text(
                                text = "Detalle del movimiento",
                                style = TextStyle(
                                    fontFamily = Fraunces,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 22.sp,
                                    color = ColorBone
                                )
                            )
                        }
                    }"""
new_topbar = """                    var showDeleteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
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
                        IconButton(onClick = { /* TODO: Edit */ }, modifier = Modifier.size(36.dp)) {
                            Icon(androidx.compose.material.icons.Icons.Filled.Edit, "Editar", tint = ColorBone)
                        }
                        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(36.dp)) {
                            Icon(androidx.compose.material.icons.Icons.Filled.Delete, "Eliminar", tint = ColorError)
                        }
                    }"""
content = content.replace(old_topbar, new_topbar)

# Remove the old Delete button logic at the bottom
old_delete_logic = """                    var showDeleteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                    
                    if (isToday) {
                        Spacer(Modifier.height(8.dp))
                        
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorClay.copy(alpha = 0.15f), contentColor = ColorClay),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = !state.isDeleting
                        ) {
                            if (state.isDeleting) {
                                CircularProgressIndicator(color = ColorClay, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Eliminar movimiento", style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp))
                            }
                        }
                    }
                    
                    if (showDeleteDialog) {"""
new_delete_logic = """                    if (showDeleteDialog) {"""
content = content.replace(old_delete_logic, new_delete_logic)

# Remove the isToday logic entirely
old_is_today = """                    val today = java.time.LocalDate.now()
                    val txDate = runCatching { Instant.parse(tx.date).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()
                    val isToday = txDate == today"""
content = content.replace(old_is_today, "")

# Modify Card 1 (Amount and Type) to include the category icon
old_card_1 = """                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = iconText, color = amountColor, fontSize = 20.sp)
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
                        }"""
new_card_1 = """                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (state.categoryIcon.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color(0xFF162B28)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = co.privado.finly.util.toIcon(state.categoryIcon),
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
                        }"""
content = content.replace(old_card_1, new_card_1)

# Add imports for Icons
if 'import androidx.compose.material.icons.filled.Delete' not in content:
    content = content.replace('import androidx.compose.material.icons.filled.Warning', 'import androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.Edit')

with open('app/src/main/java/co/privado/finly/ui/screens/transaction_detail/TransactionDetailScreen.kt', 'w') as f:
    f.write(content)
