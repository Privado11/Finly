import sys

with open("app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt", "r") as f:
    content = f.read()

start_marker = "    if (showTransactionTypeSheet) {"
if start_marker in content:
    pre_content = content[:content.index(start_marker)]
    
    new_bs = """    if (showTransactionTypeSheet) {
        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showTransactionTypeSheet = false },
            containerColor = co.privado.finly.ui.theme.ColorInk,
            sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp).padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Asistente Inteligente Button
                Surface(
                    onClick = { /* TODO: Asistente IA */ },
                    shape = RoundedCornerShape(24.dp),
                    color = co.privado.finly.ui.theme.ColorSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E4B4B)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFF5CD5B5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Filled.AutoAwesome, contentDescription = null, tint = co.privado.finly.ui.theme.ColorInk, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Asistente Inteligente", fontFamily = co.privado.finly.ui.theme.Inter, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = co.privado.finly.ui.theme.ColorBone)
                            Spacer(Modifier.height(4.dp))
                            Text("Dicta, toma una foto o escribe", fontFamily = co.privado.finly.ui.theme.Inter, fontSize = 14.sp, color = co.privado.finly.ui.theme.ColorSlate)
                        }
                        Icon(androidx.compose.material.icons.Icons.Filled.ChevronRight, contentDescription = null, tint = co.privado.finly.ui.theme.ColorSlate)
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        onClick = { showTransactionTypeSheet = false; nav.navigate("${Routes.AddTransaction}/expense") },
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = co.privado.finly.ui.theme.ColorSurface
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(co.privado.finly.ui.theme.ColorClay.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(androidx.compose.material.icons.Icons.Filled.TrendingDown, contentDescription = null, tint = co.privado.finly.ui.theme.ColorClay, modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Gasto", fontFamily = co.privado.finly.ui.theme.Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = co.privado.finly.ui.theme.ColorBone)
                            Spacer(Modifier.height(4.dp))
                            Text("Manual", fontFamily = co.privado.finly.ui.theme.Inter, fontSize = 14.sp, color = co.privado.finly.ui.theme.ColorSlate)
                        }
                    }
                    Surface(
                        onClick = { showTransactionTypeSheet = false; nav.navigate("${Routes.AddTransaction}/income") },
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = co.privado.finly.ui.theme.ColorSurface
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(co.privado.finly.ui.theme.ColorMoss.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(androidx.compose.material.icons.Icons.Filled.TrendingUp, contentDescription = null, tint = co.privado.finly.ui.theme.ColorMoss, modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Ingreso", fontFamily = co.privado.finly.ui.theme.Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = co.privado.finly.ui.theme.ColorBone)
                            Spacer(Modifier.height(4.dp))
                            Text("Manual", fontFamily = co.privado.finly.ui.theme.Inter, fontSize = 14.sp, color = co.privado.finly.ui.theme.ColorSlate)
                        }
                    }
                }
            }
        }
    }
}
"""
    with open("app/src/main/java/co/privado/finly/ui/navigation/MainScreen.kt", "w") as f:
        f.write(pre_content + new_bs)
    print("Done rewriting")
else:
    print("Start marker not found")
