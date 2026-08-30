package co.privado.finly.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.privado.finly.domain.model.ReviewQueueItem
import co.privado.finly.domain.repository.ReviewQueueRepository
import co.privado.finly.ui.state.GlobalMessageNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewUiState(
    val isLoading: Boolean = true,
    val pendingItems: List<ReviewQueueItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewQueueRepository,
    private val globalMessageNotifier: GlobalMessageNotifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPending()
    }

    private fun loadPending() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            reviewRepository.getPending().fold(
                onSuccess = { items ->
                    _uiState.value = _uiState.value.copy(isLoading = false, pendingItems = items)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = err.message)
                }
            )
        }
    }

    fun markAsResolved(id: String, message: String) {
        viewModelScope.launch {
            reviewRepository.markResolved(id).fold(
                onSuccess = {
                    globalMessageNotifier.showMessage(message)
                    loadPending() // Refresh list
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(error = err.message)
                }
            )
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
