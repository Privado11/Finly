with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'r') as f:
    content = f.read()

# Add missing imports
if "import androidx.compose.ui.text.TextStyle" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport androidx.compose.ui.text.TextStyle\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.foundation.border")

# Remove fully qualified names
content = content.replace("androidx.compose.ui.text.TextStyle", "TextStyle")
content = content.replace(".androidx.compose.ui.draw.clip", ".clip")
content = content.replace(".androidx.compose.foundation.border", ".border")
content = content.replace("androidx.compose.ui.graphics.Color.Transparent", "Color.Transparent")

with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'w') as f:
    f.write(content)
