with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'r') as f:
    content = f.read()

# Replace header
old_header = """            Text("CATEGORÍAS", style = TypographyEyebrow, color = ColorBrass, modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 4.dp, top = 24.dp))"""
new_header = """            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)) {
                Text(
                    text = "CATEGORÍAS",
                    style = TypographyEyebrow,
                    color = ColorBrass,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Organiza tu dinero",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Medium,
                        fontSize = 26.sp,
                        color = ColorBone
                    )
                )
            }
            
            var selectedTab by rememberSaveable { mutableStateOf(CategoryType.expense) }
            
            // Segmented control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
                    .androidx.compose.ui.draw.clip(RoundedCornerShape(16.dp))
                    .background(ColorSurface)
                    .androidx.compose.foundation.border(1.dp, ColorHair, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                val options = listOf(
                    CategoryType.expense to "Gastos",
                    CategoryType.income to "Ingresos",
                )
                options.forEach { (optType, label) ->
                    val selected = selectedTab == optType
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .androidx.compose.ui.draw.clip(RoundedCornerShape(12.dp))
                            .background(if (selected) ColorBrass else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { selectedTab = optType }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selected) Color(0xFF1A1305) else ColorSlate
                            )
                        )
                    }
                }
            }
"""
content = content.replace(old_header, new_header)

# Modify LazyColumn iteration
old_lazy = """                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CategoryType.values().forEach { type ->
                        val mainCategories = state.categories.filter { it.type == type && it.parentId == null }
                        if (mainCategories.isNotEmpty()) {
                            item { Text(type.sectionLabel(), fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ColorSlate) }
                            mainCategories.forEach { parent ->
                                item(key = parent.id) { 
                                    Text(parent.name, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = ColorBone, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                                }
                                val subcategories = state.categories.filter { it.parentId == parent.id }
                                items(subcategories, key = { it.id }) { category -> 
                                    CategoryCard(category, onDelete = { viewModel.showDeleteDialog(category) }) 
                                }
                            }
                        }
                    }
                }"""

new_lazy = """                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 0.dp, 20.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val mainCategories = state.categories.filter { it.type == selectedTab && it.parentId == null }
                    if (mainCategories.isNotEmpty()) {
                        mainCategories.forEach { parent ->
                            item(key = parent.id) { 
                                Text(parent.name, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = ColorBone, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                            }
                            val subcategories = state.categories.filter { it.parentId == parent.id }
                            items(subcategories, key = { it.id }) { category -> 
                                CategoryCard(category, onDelete = { viewModel.showDeleteDialog(category) }) 
                            }
                        }
                    }
                }"""
content = content.replace(old_lazy, new_lazy)

with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'w') as f:
    f.write(content)
