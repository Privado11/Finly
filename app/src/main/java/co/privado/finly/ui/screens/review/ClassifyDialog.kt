package co.privado.finly.ui.screens.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.privado.finly.domain.model.AccountBalance
import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.ReviewQueueItem
import co.privado.finly.domain.model.TransactionType
import co.privado.finly.ui.screens.transactions.FinlyPickerField
import co.privado.finly.ui.screens.transactions.FinlyTextField
import co.privado.finly.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassifyDialog(
    item: ReviewQueueItem,
    accounts: List<AccountBalance>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Double, String, String?, TransactionType, String?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    var selectedAccount by remember { mutableStateOf<AccountBalance?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    
    var showAccountPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val isExpense = true 
    
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
                
                FinlyTextField(
                    label = "Monto (COP)",
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                    placeholder = "$0",
                    isBig = true,
                    keyboardType = KeyboardType.Decimal
                )
                
                Spacer(Modifier.height(12.dp))
                
                FinlyTextField(
                    label = "Comercio / Descripción",
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "",
                    isBig = false,
                    keyboardType = KeyboardType.Text
                )
                
                Spacer(Modifier.height(12.dp))
                
                FinlyPickerField(
                    label = "Cuenta",
                    value = selectedAccount?.name ?: "Seleccionar cuenta",
                    isPlaceholder = selectedAccount == null,
                    onClick = { showAccountPicker = true }
                )
                
                Spacer(Modifier.height(4.dp))
                
                FinlyPickerField(
                    label = "Categoría",
                    value = selectedCategory?.name ?: "Sin categoría",
                    isPlaceholder = selectedCategory == null,
                    onClick = { showCategoryPicker = true }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount != null && selectedAccount != null) {
                        onSave(parsedAmount, selectedAccount!!.id!!, selectedCategory?.id, TransactionType.expense, description.takeIf { it.isNotBlank() })
                    }
                },
                enabled = amount.isNotBlank() && selectedAccount != null,
                colors = ButtonDefaults.buttonColors(containerColor = ColorBrass, contentColor = ColorOnBrass, disabledContainerColor = ColorHair, disabledContentColor = ColorSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar", fontFamily = Inter, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontFamily = Inter, color = ColorSlate)
            }
        }
    )

    if (showAccountPicker) {
        AlertDialog(
            onDismissRequest = { showAccountPicker = false }, 
            title = { Text("Selecciona una cuenta") }, 
            text = { 
                Column { 
                    accounts.forEach { account ->
                        TextButton(onClick = { selectedAccount = account; showAccountPicker = false }, modifier = Modifier.fillMaxWidth()) { Text(account.name) } 
                    } 
                } 
            }, 
            confirmButton = { TextButton(onClick = { showAccountPicker = false }) { Text("Cancelar") } }
        )
    }

    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false }, 
            title = { Text("Selecciona una categoría") }, 
            text = { 
                Column { 
                    TextButton(onClick = { selectedCategory = null; showCategoryPicker = false }, modifier = Modifier.fillMaxWidth()) { Text("Sin categoría") }
                    categories.forEach { cat ->
                        TextButton(onClick = { selectedCategory = cat; showCategoryPicker = false }, modifier = Modifier.fillMaxWidth()) { Text(cat.name) } 
                    } 
                } 
            }, 
            confirmButton = { TextButton(onClick = { showCategoryPicker = false }) { Text("Cancelar") } }
        )
    }
}
