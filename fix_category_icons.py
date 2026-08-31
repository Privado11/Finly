import re

# Update HomeViewModel
with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val categoryNames: Map<String, String> = emptyMap()',
    'val categoryNames: Map<String, String> = emptyMap(),\n    val categoryIcons: Map<String, String> = emptyMap()'
)

content = content.replace(
    'categoryNames = categoryNames.mapValues { it.value.name }',
    'categoryNames = categoryNames.mapValues { it.value.name }, categoryIcons = categoryNames.mapValues { it.value.icon }'
)

with open('app/src/main/java/co/privado/finly/ui/screens/home/HomeViewModel.kt', 'w') as f:
    f.write(content)


# Update HistoryViewModel
with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val categoryNames: Map<String, String> = emptyMap(),',
    'val categoryNames: Map<String, String> = emptyMap(),\n    val categoryIcons: Map<String, String> = emptyMap(),'
)

content = content.replace(
    'val categoryNames = categories.associate { it.id!! to it.name }',
    'val categoryNames = categories.associate { it.id!! to it.name }\n        val categoryIcons = categories.associate { it.id!! to it.icon }'
)

content = content.replace(
    'categoryNames = categoryNames,',
    'categoryNames = categoryNames,\n            categoryIcons = categoryIcons,'
)

with open('app/src/main/java/co/privado/finly/ui/screens/history/HistoryViewModel.kt', 'w') as f:
    f.write(content)
