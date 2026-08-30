package co.privado.finly.ui.screens.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.privado.finly.domain.model.ReviewQueueItem
import co.privado.finly.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassifyDialog(item: ReviewQueueItem, onDismiss: () -> Unit, onSave: () -> Unit) {
    // Valores falsos por ahora, pero extraídos del paquete en la vida real
    val amount = "0.00" 
    val desc = "Descripción del movimiento"
    
    var category by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorSurface,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Clasificar movimiento", fontFamily = Fraunces, color = ColorBone) },
        text = { 
            Column(Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(12.dp), color = ColorInk, modifier = Modifier.fillMaxWidth()) {
                    Text(item.originalText, fontFamily = Inter, fontStyle = FontStyle.Italic, fontSize = 13.sp, color = ColorClay, modifier = Modifier.padding(12.dp))
                }
                Spacer(Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = {},
                    label = { Text("Monto", fontFamily = Inter) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorBrass,
                        focusedLabelColor = ColorBrass,
                        unfocusedBorderColor = ColorHair,
                        unfocusedLabelColor = ColorSlate,
                        focusedTextColor = ColorBone,
                        unfocusedTextColor = ColorBone
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = desc,
                    onValueChange = {},
                    label = { Text("Descripción", fontFamily = Inter) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorBrass,
                        focusedLabelColor = ColorBrass,
                        unfocusedBorderColor = ColorHair,
                        unfocusedLabelColor = ColorSlate,
                        focusedTextColor = ColorBone,
                        unfocusedTextColor = ColorBone
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = item.packageName.replace("com.", "").capitalize(),
                    onValueChange = {},
                    label = { Text("Cuenta sugerida", fontFamily = Inter) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorBrass,
                        focusedLabelColor = ColorBrass,
                        unfocusedBorderColor = ColorHair,
                        unfocusedLabelColor = ColorSlate,
                        focusedTextColor = ColorBone,
                        unfocusedTextColor = ColorBone
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría (Falta)", fontFamily = Inter) },
                    placeholder = { Text("Ej. Comida, Transporte", fontFamily = Inter) },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, tint = ColorSlate) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorBrass,
                        focusedLabelColor = ColorBrass,
                        unfocusedBorderColor = ColorError,
                        unfocusedLabelColor = ColorError,
                        focusedTextColor = ColorBone,
                        unfocusedTextColor = ColorBone,
                        cursorColor = ColorBrass
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = ColorBrass, contentColor = ColorOnBrass),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar movimiento", fontFamily = Inter, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontFamily = Inter, color = ColorSlate)
            }
        }
    )
}
