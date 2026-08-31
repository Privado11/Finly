import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# Make sure we have the imports needed
imports = """
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import co.privado.finly.util.toIcon
"""
content = content.replace("import androidx.compose.ui.Alignment", imports + "\nimport androidx.compose.ui.Alignment")


new_pickers = """
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun AccountPicker(title: String, items: List<co.privado.finly.domain.model.AccountBalance>, select: (co.privado.finly.domain.model.AccountBalance) -> Unit, dismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)
    
    val consumeSwipes = Modifier.pointerInput(Unit) {
        detectVerticalDragGestures { change, _ -> change.consume() }
    }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        containerColor = ColorInk,
        dragHandle = {
            androidx.compose.material3.BottomSheetDefaults.DragHandle()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.95f)
                .padding(horizontal = 24.dp)
                .then(consumeSwipes)
        ) {
            Text(title, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = ColorBone)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                textStyle = TextStyle(color = ColorBone, fontSize = 15.sp, fontFamily = Inter),
                singleLine = true,
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
                        Column {
                            Text(acc.name, fontFamily = Inter, fontSize = 16.sp, color = ColorBone)
                            Text(formatMoney(acc.balance), fontFamily = IbmPlexMono, fontSize = 13.sp, color = ColorSlate)
                        }
                    }
                }
            }
            
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).clip(RoundedCornerShape(12.dp)).clickable { }.border(1.dp, ColorMoss, RoundedCornerShape(12.dp))) {
                Row(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nueva Cuenta", color = ColorMoss, fontSize = 15.sp, fontWeight = FontWeight.Medium)
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
    
    val consumeSwipes = Modifier.pointerInput(Unit) {
        detectVerticalDragGestures { change, _ -> change.consume() }
    }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        containerColor = ColorInk,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.95f)
                .padding(horizontal = 24.dp)
                .then(consumeSwipes)
        ) {
            Text("Categoría", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = ColorBone)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                textStyle = TextStyle(color = ColorBone, fontSize = 15.sp, fontFamily = Inter),
                singleLine = true,
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
                            val icon = cat.icon.toIcon()
                            Icon(icon ?: androidx.compose.material.icons.Icons.Filled.Category, contentDescription = null, tint = ColorBrass, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(cat.name, fontFamily = Inter, fontSize = 16.sp, color = ColorBone)
                    }
                }
            }
            
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).clip(RoundedCornerShape(12.dp)).clickable { }.border(1.dp, ColorMoss, RoundedCornerShape(12.dp))) {
                Row(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nueva Categoría", color = ColorMoss, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
"""

# Replace from `private fun AccountPicker` to just before `private fun formatMoney` (or EOF if not found)
if 'private fun formatMoney' in content:
    content = re.sub(r'private fun AccountPicker.*?(\nprivate fun formatMoney)', new_pickers + r'\1', content, flags=re.DOTALL)
else:
    content = re.sub(r'private fun AccountPicker.*', new_pickers, content, flags=re.DOTALL)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
