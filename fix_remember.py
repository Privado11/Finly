with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.runtime.remember" not in content:
    content = content.replace("import androidx.compose.runtime.saveable.rememberSaveable", "import androidx.compose.runtime.saveable.rememberSaveable\nimport androidx.compose.runtime.remember")

with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'w') as f:
    f.write(content)
