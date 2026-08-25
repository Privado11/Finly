package co.privado.finly.ui.state

import co.privado.finly.domain.model.Transaction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Evento efímero para reflejar en pantalla un movimiento ya confirmado por Supabase. */
@Singleton
class TransactionUpdateNotifier @Inject constructor() {
    private val _created = MutableSharedFlow<Transaction>(extraBufferCapacity = 1)
    val created = _created.asSharedFlow()

    fun notifyCreated(transaction: Transaction) {
        _created.tryEmit(transaction)
    }
}
