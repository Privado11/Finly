import re

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'r') as f:
    content = f.read()

# I will replace the CategoryPicker function entirely.
new_category_picker = """@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun CategoryPicker(items: List<Category>, select: (Category) -> Unit, dismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    
    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
    val peek = screenHeight * 0.6f
    
    val sheetState = androidx.compose.material3.rememberStandardBottomSheetState(
        initialValue = androidx.compose.material3.SheetValue.PartiallyExpanded,
        skipHiddenState = false
    )
    val scaffoldState = androidx.compose.material3.rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    
    androidx.compose.runtime.LaunchedEffect(sheetState.currentValue) {
        if (sheetState.currentValue == androidx.compose.material3.SheetValue.Hidden) {
            dismiss()
        }
    }
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = dismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        androidx.compose.material3.BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = peek,
            sheetContainerColor = ColorInk,
            containerColor = Color.Transparent,
            sheetDragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() },
            sheetContent = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .layout { measurable, constraints ->
                            val offset = try {
                                val o = sheetState.requireOffset()
                                if (o.isNaN()) constraints.maxHeight - peek.toPx() else o
                            } catch(e: Exception) {
                                constraints.maxHeight - peek.toPx()
                            }
                            val visibleHeight = (constraints.maxHeight - offset).toInt().coerceAtLeast(0)
                            val placeable = measurable.measure(
                                constraints.copy(minHeight = visibleHeight, maxHeight = visibleHeight)
                            )
                            layout(constraints.maxWidth, constraints.maxHeight) {
                                placeable.place(0, 0)
                            }
                        }
                        .padding(horizontal = 24.dp)
                ) {
                    Text("Categoría", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = ColorBone)
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
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
                                Icon(Icons.Filled.Search, contentDescription = null, tint = ColorSlate, modifier = Modifier.size(20.dp))
                            }
                        }
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    Text("TODAS", fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorSlate, modifier = Modifier.padding(bottom = 8.dp))
                    
                    val dummyDispatcher = remember { androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher() }
                    val dummyConnection = remember { object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {} }
                    
                    androidx.compose.foundation.lazy.LazyColumn(
                        Modifier.fillMaxWidth().weight(1f).nestedScroll(dummyConnection, dummyDispatcher)
                    ) {
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
                                            Icon(child.icon.toIcon(), contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
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
                            .border(width = 1.dp, color = ColorMoss, shape = RoundedCornerShape(12.dp))
                    ) {
                        Row(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = ColorMoss, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Nueva Categoría", color = ColorMoss, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        ) {
            Box(Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { dismiss() }) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
            }
        }
    }
}
"""

# Extract everything before CategoryPicker
match = re.search(r'@androidx\.compose\.material3\.ExperimentalMaterial3Api\n@Composable\nprivate fun CategoryPicker', content)
if match:
    before = content[:match.start()]
    with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsScreen.kt', 'w') as f:
        f.write(before + new_category_picker)
else:
    print("Match not found")

