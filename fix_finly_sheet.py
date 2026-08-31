import re
with open('app/src/main/java/co/privado/finly/ui/components/FinlySheet.kt', 'r') as f:
    content = f.read()

content = content.replace('sheetSwipeEnabled = false,', '')
# Add the pointer input to the outer box of the content
old_box = "Box(\n            modifier = Modifier\n                .fillMaxWidth()\n                .height(alturaActual)\n        ) {"
new_box = "Box(\n            modifier = Modifier\n                .fillMaxWidth()\n                .height(alturaActual)\n                .pointerInput(Unit) { detectVerticalDragGestures { change, _ -> change.consume() } }\n        ) {"
content = content.replace(old_box, new_box)

with open('app/src/main/java/co/privado/finly/ui/components/FinlySheet.kt', 'w') as f:
    f.write(content)
