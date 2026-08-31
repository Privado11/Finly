with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'r') as f:
    content = f.read()
content = content.replace('co.privado.finly.util.toIcon(catIconStr)', 'catIconStr.toIcon()')
with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryScreen.kt', 'r') as f:
    content = f.read()
content = content.replace('co.privado.finly.util.toIcon(catIconStr)', 'catIconStr.toIcon()')
with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryScreen.kt', 'w') as f:
    f.write(content)
