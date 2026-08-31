with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import TextStyle", "import androidx.compose.ui.text.TextStyle")

with open('app/src/main/java/co/privado/finly/ui/screens/categories/CategoriesScreen.kt', 'w') as f:
    f.write(content)
