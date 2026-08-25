package co.privado.finly.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.CategoryType
import co.privado.finly.domain.repository.CategoryRepository
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
    val showCreateDialog: Boolean = false
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init { loadCategories() }

    fun loadCategories() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        categoryRepository.getCategories()
            .onSuccess { categories -> _uiState.update { it.copy(categories = categories, isLoading = false) } }
            .onFailure { _uiState.update { it.copy(isLoading = false, error = "No pudimos cargar tus categorías. Revisa tu conexión e inténtalo de nuevo.") } }
    }

    fun showCreateDialog(show: Boolean) = _uiState.update { it.copy(showCreateDialog = show, error = null) }

    fun createCategory(name: String, type: CategoryType) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Escribe un nombre para la categoría.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            categoryRepository.addCategory(name, type, icon = type.defaultIcon(), color = type.defaultColor())
                .onSuccess { category -> _uiState.update { it.copy(categories = (it.categories + category).sortedBy { item -> item.name.lowercase() }, isSaving = false, showCreateDialog = false) } }
                .onFailure { _uiState.update { it.copy(isSaving = false, error = "No fue posible guardar la categoría. Inténtalo de nuevo.") } }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }
}

fun CategoryType.defaultIcon() = if (this == CategoryType.income) "arrow_upward" else "arrow_downward"
fun CategoryType.defaultColor() = if (this == CategoryType.income) "#006C51" else "#BA1A1A"
