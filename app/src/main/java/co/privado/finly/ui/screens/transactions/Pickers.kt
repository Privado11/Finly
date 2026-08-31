package co.privado.finly.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.privado.finly.domain.model.AccountBalance
import co.privado.finly.domain.model.Category
import co.privado.finly.ui.components.FinlySheet
import co.privado.finly.ui.theme.*
import co.privado.finly.util.toIcon

// ---------- Piezas comunes reutilizadas por ambos pickers ----------

@Composable
private fun PickerBuscador(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = TextStyle(color = ColorBone, fontSize = 15.sp, fontFamily = Inter),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ColorHair,
            unfocusedBorderColor = ColorHair,
            cursorColor = ColorBrass
        ),
        placeholder = { Text(placeholder, color = ColorSlate, fontSize = 15.sp, fontFamily = Inter) },
        trailingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null, tint = ColorSlate, modifier = Modifier.size(20.dp))
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorSurface)
            
    )
}

@Composable
private fun PickerBotonAgregar(texto: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, ColorMoss, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(texto, color = ColorMoss, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ---------- AccountPicker ----------

@Composable
fun AccountPicker(
    title: String,
    items: List<AccountBalance>,
    select: (AccountBalance) -> Unit,
    dismiss: () -> Unit,
    onAgregarCuenta: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
        val filtrados = items.filter { it.name.contains(searchQuery, ignoreCase = true) }

    FinlySheet(onDismissRequest = dismiss) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Text(title, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = ColorBone)
            Spacer(Modifier.height(16.dp))
            PickerBuscador(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar cuenta..."
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "TODAS", fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = ColorSlate, modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(filtrados) { acc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { select(acc) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(ColorSurfaceHi),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = ColorBrass, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(acc.name, fontFamily = Inter, fontSize = 16.sp, color = ColorBone)
                            Text(formatMoney(acc.balance), fontFamily = IbmPlexMono, fontSize = 13.sp, color = ColorSlate)
                        }
                    }
                }
            }

            // Botón de nueva cuenta visible siempre
            PickerBotonAgregar("Nueva Cuenta", onAgregarCuenta)
        }
    }
}

// ---------- CategoryPicker ----------

@Composable
fun CategoryPicker(
    items: List<Category>,
    select: (Category) -> Unit,
    dismiss: () -> Unit,
    onAgregarCategoria: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
        val parents = items.filter { it.parentId == null }

    FinlySheet(onDismissRequest = dismiss) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Text("Categoría", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = ColorBone)
            Spacer(Modifier.height(16.dp))
            PickerBuscador(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar categoría..."
            )
            Spacer(Modifier.height(24.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                parents.forEach { parent ->
                    val children = items.filter {
                        it.parentId == parent.id && it.name.contains(searchQuery, ignoreCase = true)
                    }
                    if (children.isNotEmpty() || parent.name.contains(searchQuery, ignoreCase = true)) {
                        item {
                            Text(
                                parent.name.uppercase(), fontFamily = Inter, fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold, color = ColorSlate,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(children) { child ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { select(child) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF162B28)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(child.icon.toIcon(), contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(child.name, fontFamily = Inter, fontSize = 16.sp, color = ColorBone)
                            }
                        }
                    }
                }
            }

            // Botón de nueva categoría visible siempre
            PickerBotonAgregar("Nueva Categoría", onAgregarCategoria)
        }
    }
}

private fun formatMoney(amount: Double): String {
    val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "CO"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}
