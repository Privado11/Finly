with open('app/src/main/java/co/privado/finly/ui/components/FinlySheet.kt', 'r') as f:
    content = f.read()

# Remove the pointerInput block on the Box
pointer_input_str = ".pointerInput(Unit) { detectVerticalDragGestures { change, _ -> change.consume() } }"
content = content.replace(pointer_input_str, "")

# Add sheetGesturesEnabled
content = content.replace(
    'dragHandle = null,',
    'sheetGesturesEnabled = false,\n        dragHandle = null,'
)

with open('app/src/main/java/co/privado/finly/ui/components/FinlySheet.kt', 'w') as f:
    f.write(content)
