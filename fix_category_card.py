with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'r') as f:
    content = f.read()

# 1. Reduce LazyColumn spacing
content = content.replace("Arrangement.spacedBy(16.dp)", "Arrangement.spacedBy(10.dp)")

# 2. Modify CategoryCard
old_card = """@Composable private fun CategoryCard(category: Category, onDelete: () -> Unit) {
    val color = if (category.type == CategoryType.income) ColorMoss else ColorClay
    Surface(shape = RoundedCornerShape(20.dp), color = ColorSurface) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(category.icon.toIcon(), contentDescription = null, tint = color)
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(category.name, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = ColorBone)
            }
            if (category.userId != null) {
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = ColorSlate)
                }
            }
        }
    }
}"""

new_card = """@Composable private fun CategoryCard(category: Category, onDelete: () -> Unit) {
    val color = if (category.type == CategoryType.income) ColorMoss else ColorClay
    Surface(shape = RoundedCornerShape(16.dp), color = ColorSurface) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(category.icon.toIcon(), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(category.name, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = ColorBone)
            }
            if (category.userId != null) {
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = ColorSlate, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}"""

content = content.replace(old_card, new_card)

with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'w') as f:
    f.write(content)
