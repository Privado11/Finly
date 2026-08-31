with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'color = ColorBrass\n                    ),\n                    modifier = Modifier.clickable { onNavigateToHistory() }',
    'color = ColorSlate\n                    ),\n                    modifier = Modifier.clickable { onNavigateToHistory() }'
)

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'w') as f:
    f.write(content)
