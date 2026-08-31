package co.privado.finly.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.CategoryType
import co.privado.finly.domain.repository.CategoryRepository
import co.privado.finly.ui.state.GlobalMessageNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val categoryToDelete: Category? = null
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val globalMessageNotifier: GlobalMessageNotifier
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init { loadCategories() }

    fun loadCategories() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        categoryRepository.getCategories()
            .onSuccess { categories -> _uiState.update { it.copy(categories = categories, isLoading = false) } }
            .onFailure { error -> _uiState.update { it.copy(isLoading = false, error = readableError(error)) } }
    }

    fun showCreateDialog(show: Boolean) = _uiState.update { it.copy(showCreateDialog = show, error = null) }
    
    fun showDeleteDialog(category: Category?) = _uiState.update { it.copy(categoryToDelete = category, error = null) }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            categoryRepository.deleteCategory(category.id)
                .onSuccess { 
                    _uiState.update { state -> 
                        state.copy(
                            categories = state.categories.filter { it.id != category.id }, 
                            isSaving = false, 
                            categoryToDelete = null 
                        ) 
                    }
                    globalMessageNotifier.showMessage("Categoría eliminada")
                }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, error = readableError(error), categoryToDelete = null) } }
        }
    }

    fun createCategory(name: String, type: CategoryType, parentId: String?, icon: String?) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Escribe un nombre para la categoría.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            categoryRepository.addCategory(name, type, parentId, icon, type.defaultColor())
                .onSuccess { category -> 
                    _uiState.update { it.copy(categories = (it.categories + category).sortedBy { item -> item.name.lowercase() }, isSaving = false, showCreateDialog = false) }
                    globalMessageNotifier.showMessage("Categoría creada")
                }
                .onFailure { error -> _uiState.update { it.copy(isSaving = false, error = readableError(error)) } }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }

    private fun readableError(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("network", ignoreCase = true) || message.contains("resolve", ignoreCase = true) -> "No pudimos conectar con Finly. Revisa tu conexión."
            message.contains("foreign key", ignoreCase = true) || message.contains("violates", ignoreCase = true) -> "No puedes eliminar esta categoría porque tiene movimientos registrados."
            else -> "No fue posible completar la acción. Inténtalo de nuevo."
        }
    }
}

fun CategoryType.defaultIcon() = if (this == CategoryType.income) "arrow_upward" else "arrow_downward"
fun CategoryType.defaultColor() = if (this == CategoryType.income) "#006C51" else "#BA1A1A"
