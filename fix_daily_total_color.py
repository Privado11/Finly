with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryScreen.kt', 'r') as f:
    content = f.read()

old_color_line = 'val color = if (dailyTotal > 0) ColorMoss else if (dailyTotal < 0) ColorClay else ColorSlate'
new_color_line = 'val color = if (dailyTotal > 0) ColorMoss else if (dailyTotal < 0) ColorBone else ColorSlate'

content = content.replace(old_color_line, new_color_line)

with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryScreen.kt', 'w') as f:
    f.write(content)
