import re

with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val amountColor = if (isInc) ColorMoss else if (isExp) ColorClay else ColorBone',
    'val amountColor = if (isInc) ColorMoss else ColorBone'
)

with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryScreen.kt', 'w') as f:
    f.write(content)
