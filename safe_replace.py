import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Make sure imports are present
imports = """
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
"""
content = content.replace("import androidx.compose.ui.Alignment", imports + "\nimport androidx.compose.ui.Alignment")

# 1. Add OptIn at the top, just below package
content = content.replace('package co.privado.finly.ui.screens.transactions', 'package co.privado.finly.ui.screens.transactions\n\n@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)')

# 2. Add initialType to TransactionsScreen
content = content.replace(
    'fun TransactionsScreen(onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel())',
    'fun TransactionsScreen(initialType: co.privado.finly.domain.model.TransactionType = co.privado.finly.domain.model.TransactionType.expense, onSaved: () -> Unit = {}, onBack: () -> Unit = {}, viewModel: TransactionsViewModel = hiltViewModel())'
)
content = content.replace(
    'var type by rememberSaveable { mutableStateOf(TransactionType.expense) }',
    'var type by rememberSaveable { mutableStateOf(initialType) }'
)

# 3. Change Account -> AccountBalance
content = content.replace('List<Account>', 'List<co.privado.finly.domain.model.AccountBalance>')
content = content.replace('(Account)', '(co.privado.finly.domain.model.AccountBalance)')
content = content.replace('var account by remember { mutableStateOf<Account?>(null) }', 'var account by remember { mutableStateOf<co.privado.finly.domain.model.AccountBalance?>(null) }')
content = content.replace('var destination by remember { mutableStateOf<Account?>(null) }', 'var destination by remember { mutableStateOf<co.privado.finly.domain.model.AccountBalance?>(null) }')
content = content.replace('val state by viewModel.uiState.collectAsState()', 'val state by viewModel.state.collectAsState()')
content = content.replace('viewModel.save(type, amount, account, destination, category, merchant, date)', 'viewModel.save(type, amount, account, category, merchant, date, null)')

# 4. Remove transfer
content = re.sub(r'TransactionType\.transfer to "Transferencia",?\s*', '', content)
content = re.sub(r'if \(type == TransactionType\.transfer\) \{.*?\}', '', content, flags=re.DOTALL)
old_cat = """                // Categoría (si no es transferencia)
                if (type != TransactionType.transfer) {
                    FinlyPickerField(
                        label = "Categoría",
                        value = category?.name ?: "Sin categoría",
                        isPlaceholder = category == null,
                        onClick = { picker = "category" }
                    )
                }"""
new_cat = """                // Categoría
                FinlyPickerField(
                    label = "Categoría",
                    value = category?.name ?: "Sin categoría",
                    isPlaceholder = category == null,
                    onClick = { picker = "category" }
                )"""
content = content.replace(old_cat, new_cat)

# 5. Locate the Pickers
start_str = "private fun AccountPicker("
end_str = "private fun formatMoney("

start_idx = content.find(start_str)
end_idx = content.find(end_str)

if start_idx != -1 and end_idx != -1:
    before = content[:start_idx]
    after = content[end_idx:]
    
    pickers_code = """
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun AccountPicker(title: String, items: List<co.privado.finly.domain.model.AccountBalance>, select: (co.privado.finly.domain.model.AccountBalance) -> Unit, dismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val consumeSwipes = Modifier.pointerInput(Unit) { detectVerticalDragGestures { change, _ -> change.consume() } }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss, sheetState = sheetState, containerColor = ColorInk,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxHeight().padding(horizontal = 24.dp).then(consumeSwipes)) {
            Text(title, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = ColorBone)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it }, textStyle = TextStyle(color = ColorBone, fontSize = 15.sp, fontFamily = Inter), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorHair, unfocusedBorderColor = ColorHair, cursorColor = ColorBrass),
                placeholder = { Text("Buscar cuenta...", color = ColorSlate, fontSize = 15.sp, fontFamily = Inter) },
                trailingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = ColorSlate, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ColorSurface)
            )
            Spacer(Modifier.height(24.dp))
            Text("TODAS", fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorSlate, modifier = Modifier.padding(bottom = 8.dp))
            androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }
                items(filtered) { acc ->
                    Row(Modifier.fillMaxWidth().clickable { select(acc) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(ColorSurfaceHi), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = ColorBrass, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column { Text(acc.name, fontFamily = Inter, fontSize = 16.sp, color = ColorBone); Text(formatMoney(acc.balance), fontFamily = IbmPlexMono, fontSize = 13.sp, color = ColorSlate) }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).clip(RoundedCornerShape(12.dp)).clickable { }.border(1.dp, ColorMoss, RoundedCornerShape(12.dp))) {
                Row(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp)); Text("Nueva Cuenta", color = ColorMoss, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun CategoryPicker(items: List<co.privado.finly.domain.model.Category>, select: (co.privado.finly.domain.model.Category) -> Unit, dismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val consumeSwipes = Modifier.pointerInput(Unit) { detectVerticalDragGestures { change, _ -> change.consume() } }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss, sheetState = sheetState, containerColor = ColorInk,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxHeight().padding(horizontal = 24.dp).then(consumeSwipes)) {
            Text("Categoría", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = ColorBone)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it }, textStyle = TextStyle(color = ColorBone, fontSize = 15.sp, fontFamily = Inter), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorHair, unfocusedBorderColor = ColorHair, cursorColor = ColorBrass),
                placeholder = { Text("Buscar categoría...", color = ColorSlate, fontSize = 15.sp, fontFamily = Inter) },
                trailingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = ColorSlate, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ColorSurface)
            )
            Spacer(Modifier.height(24.dp))
            Text("TODAS", fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorSlate, modifier = Modifier.padding(bottom = 8.dp))
            androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }
                items(filtered) { cat ->
                    Row(Modifier.fillMaxWidth().clickable { select(cat) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(ColorSurfaceHi), contentAlignment = Alignment.Center) {
                            val icon = co.privado.finly.util.toIcon(cat.icon)
                            Icon(icon ?: Icons.Filled.Category, contentDescription = null, tint = ColorBrass, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp)); Text(cat.name, fontFamily = Inter, fontSize = 16.sp, color = ColorBone)
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).clip(RoundedCornerShape(12.dp)).clickable { }.border(1.dp, ColorMoss, RoundedCornerShape(12.dp))) {
                Row(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp)); Text("Nueva Categoría", color = ColorMoss, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
"""
    content = before + pickers_code + "\n" + after
    
with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
