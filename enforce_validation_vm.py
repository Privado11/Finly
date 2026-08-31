with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'sourceId == null -> _uiState.update { it.copy(error = "Selecciona una cuenta.") }',
    'sourceId == null -> _uiState.update { it.copy(error = "Selecciona una cuenta.") }\n            categoryId == null -> _uiState.update { it.copy(error = "Selecciona una categoría.") }'
)

with open('app/src/main/java/co/privado/finly/ui/screens/transactions/TransactionsViewModel.kt', 'w') as f:
    f.write(content)
