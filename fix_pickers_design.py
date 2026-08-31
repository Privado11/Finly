import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# I need to rewrite AccountPicker and CategoryPicker.
# The user wants them to look like the image.

category_picker = """@Composable
private fun CategoryPicker(items: List<Category>, select: (Category) -> Unit, dismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss,
        containerColor = ColorInk,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false),
        modifier = Modifier.fillMaxHeight(0.9f) // Allow it to be taller if needed, or 0.85f
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Text("Categoría", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = ColorBone)
            Spacer(Modifier.height(16.dp))
            
            // Search Bar
            androidx.compose.foundation.text.BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(color = ColorBone, fontSize = 15.sp, fontFamily = Inter),
                singleLine = true,
                cursorBrush = SolidColor(ColorBrass),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorSurface)
                            .border(1.dp, ColorHair, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text("Buscar categoría...", color = ColorSlate, fontSize = 15.sp, fontFamily = Inter, modifier = Modifier.weight(1f))
                        } else {
                            Box(modifier = Modifier.weight(1f)) { innerTextField() }
                        }
                        Icon(androidx.compose.material.icons.Icons.Rounded.Search, contentDescription = null, tint = ColorSlate, modifier = Modifier.size(20.dp))
                    }
                }
            )
            
            Spacer(Modifier.height(24.dp))
            Text("TODAS", fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorSlate, modifier = Modifier.padding(bottom = 8.dp))
            
            androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                val parents = items.filter { it.parentId == null }
                parents.forEach { parent ->
                    val children = items.filter { it.parentId == parent.id && it.name.contains(searchQuery, ignoreCase = true) }
                    if (children.isNotEmpty() || parent.name.contains(searchQuery, ignoreCase = true)) {
                        item {
                            Text(parent.name.uppercase(), fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ColorSlate, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                        }
                        items(children) { child ->
                            Row(Modifier.fillMaxWidth().clickable { select(child) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF162B28)), contentAlignment = Alignment.Center) {
                                    Icon(co.privado.finly.util.toIcon(child.icon), contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(child.name, fontFamily = Inter, fontSize = 16.sp, color = ColorBone)
                            }
                        }
                    }
                }
            }
            
            // Dashed button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { }
                    .border(
                        width = 1.dp,
                        color = ColorMoss,
                        shape = RoundedCornerShape(12.dp)
                    ) // Wait, dashed border is tricky in standard compose. A solid border is close enough unless we use a custom drawBehind.
            ) {
                Row(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(androidx.compose.material.icons.Icons.Rounded.Add, contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nueva Categoría", color = ColorMoss, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}"""

account_picker = """@Composable
private fun AccountPicker(title: String, items: List<AccountBalance>, select: (AccountBalance) -> Unit, dismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss,
        containerColor = ColorInk,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false),
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Text(title, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = ColorBone)
            Spacer(Modifier.height(16.dp))
            
            // Search Bar
            androidx.compose.foundation.text.BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(color = ColorBone, fontSize = 15.sp, fontFamily = Inter),
                singleLine = true,
                cursorBrush = SolidColor(ColorBrass),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorSurface)
                            .border(1.dp, ColorHair, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text("Buscar cuenta...", color = ColorSlate, fontSize = 15.sp, fontFamily = Inter, modifier = Modifier.weight(1f))
                        } else {
                            Box(modifier = Modifier.weight(1f)) { innerTextField() }
                        }
                        Icon(androidx.compose.material.icons.Icons.Rounded.Search, contentDescription = null, tint = ColorSlate, modifier = Modifier.size(20.dp))
                    }
                }
            )
            
            Spacer(Modifier.height(24.dp))
            Text("TODAS", fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorSlate, modifier = Modifier.padding(bottom = 8.dp))
            
            androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }
                items(filtered) { acc ->
                    Row(Modifier.fillMaxWidth().clickable { select(acc) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(ColorSurfaceHi), contentAlignment = Alignment.Center) {
                            Icon(androidx.compose.material.icons.Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = ColorBrass, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(acc.name, fontFamily = Inter, fontSize = 16.sp, color = ColorBone)
                            Text(co.privado.finly.util.formatMoney(acc.balance), fontFamily = IbmPlexMono, fontSize = 13.sp, color = ColorSlate)
                        }
                    }
                }
            }
            
            // Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { }
                    .border(
                        width = 1.dp,
                        color = ColorMoss,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(androidx.compose.material.icons.Icons.Rounded.Add, contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nueva Cuenta", color = ColorMoss, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}"""

# Replace old pickers with new ones
import re
content = re.sub(r'@Composable\nprivate fun AccountPicker.*?\}', '', content, flags=re.DOTALL)
content = re.sub(r'@Composable\nprivate fun CategoryPicker.*?\}', '', content, flags=re.DOTALL)

content += "\n" + account_picker + "\n" + category_picker

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
    f.write(content)
