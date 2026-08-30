package co.privado.finly.ui.state

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalMessageNotifier @Inject constructor() {
    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message = _message.asSharedFlow()

    fun showMessage(msg: String) {
        _message.tryEmit(msg)
    }
}
