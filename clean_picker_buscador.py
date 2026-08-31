with open('app/src/main/java/co/privado/finly/ui/screens/transactions/Pickers.kt', 'r') as f:
    content = f.read()

content = content.replace(""",
    onFocusChange: (Boolean) -> Unit = {}""", "")

# Just in case there is `import androidx.compose.ui.focus.onFocusChanged`
content = content.replace("import androidx.compose.ui.focus.onFocusChanged\n", "")

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/Pickers.kt', 'w') as f:
    f.write(content)
