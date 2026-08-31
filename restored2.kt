<truncated 197 lines>
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurface)
            .border(1.dp, ColorHair, RoundedCornerShape(16.dp))
            .padding(horizontal = 17.dp, vertical = 15.dp)
    ) {
        Column {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = IbmPlexMono,
                    fontSize = 10.5.sp,
                    letterSpacing = 0.07.sp,
                    color = ColorSlate
                ),
                modifier = Modifier.padding(bottom = 5.dp)
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontFamily = if (isBig) Fraunces else Inter,
                    fontSize = if (isBig) 26.sp else 15.sp,
                    color = ColorBone
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                cursorBrush = SolidColor(ColorBrass),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontFamily = if (isBig) Fraunces else Inter,
                                fontSize = if (isBig) 26.sp else 15.sp,
                                color = Color(0xFF4C555F)
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun FinlyPickerField(
    label: String,
    value: String,
    isPlaceholder: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurface)
            .border(1.dp, ColorHair, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 17.dp, vertical = 15.dp)
    ) {
        Column {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = IbmPlexMono,
                    fontSize = 10.5.sp,
                    letterSpacing = 0.07.sp,
                    color = ColorSlate
                ),
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    color = if (isPlaceholder) Color(0xFF4C555F) else ColorBone
                )
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun AccountPicker(title: String, items: List<AccountBalance>, select: (AccountBalance) -> Unit, dismiss: () -> Unit) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss,
        containerColor = ColorInk,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Text(title, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = ColorBone)
            Spacer(Modifier.height(24.dp))
            LazyColumn(Modifier.fillMaxWidth()) {
                items(items) { item ->
                    Row(Modifier.fillMaxWidth().clickable { select(item) }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(ColorSurface), contentAlignment = Alignment.Center) {
                            Icon(androidx.compose.material.icons.Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = ColorBrass, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(item.name, fontFamily = Inter, fontSize = 16.sp, color = ColorBone, fontWeight = FontWeight.SemiBold)
                            Text("$${item.balance}", fontFamily = Inter, fontSize = 14.sp, color = ColorSlate)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPicker(items: List<Category>, select: (Category) -> Unit, dismiss: () -> Unit) {
    var searchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = dismiss,
        containerColor = ColorInk,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Text("Categoría", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = ColorBone)
            Spacer(Modifier.height(16.dp))
            androidx.compose.material3.OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar categoría...", color = ColorSlate) },
                trailingIcon = { Icon(androidx.compose.material.icons.Icons.Filled.Search, null, tint = ColorSlate) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = ColorHair,
                    focusedBorderColor = ColorBrass,
                    unfocusedContainerColor = ColorSurface,
                    focusedContainerColor = ColorSurface,
                    focusedTextColor = ColorBone,
                    unfocusedTextColor = ColorBone
                ),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))
            
            Text("TODAS", fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorSlate, modifier = Modifier.padding(bottom = 8.dp))
            
            LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                val parents = items.filter { it.parentId == null }
                parents.forEach { parent ->
                    val children = items.filter { it.parentId == parent.id && it.name.contains(searchQuery, ignoreCase = true) }
                    if (children.isNotEmpty() || parent.name.contains(searchQuery, ignoreCase = true)) {
                        item {
                            Text(parent.name.uppercase(), fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ColorSlate, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                        }
                        items(children) { child ->
                            Row(Modifier.fillMaxWidth().clickable { select(child) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF162B28)), contentAlignment = Alignment.Center) {
                                    Icon(child.icon.toIcon(), contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(child.name, fontFamily = Inter, fontSize = 16.sp, color = ColorBone)
                            }
                        }
                    }
                }
            }
        }
    }
}


