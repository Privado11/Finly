package co.privado.finly.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import co.privado.finly.MainActivity
import co.privado.finly.R
import co.privado.finly.data.local.SessionDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificadorApp @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionStore: SessionDataStore
) {
    private fun createIntent(destino: String, txId: String? = null): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destino", destino)
            txId?.let { putExtra("transactionId", it) }
        }
        val reqCode = txId?.hashCode() ?: destino.hashCode()
        return PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun notificar(canal: String, id: Int, titulo: String, texto: String, destino: String? = null, txId: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!sessionStore.areAppNotificationsEnabled()) return@launch
            
            val builder = NotificationCompat.Builder(context, canal)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                
            if (destino != null) builder.setContentIntent(createIntent(destino, txId))
            
            try { NotificationManagerCompat.from(context).notify(id, builder.build()) } catch (_: SecurityException) {}
        }
    }

    fun ingresoRegistrado(monto: String, comercio: String?, txId: String) =
        notificar(CanalesNotificacion.MOVIMIENTOS, txId.hashCode(), "Ingreso registrado", "+$monto${comercio?.let { " · $it" } ?: ""}", "transaction_detail", txId)

    fun egresoRegistrado(monto: String, comercio: String?, txId: String) =
        notificar(CanalesNotificacion.MOVIMIENTOS, txId.hashCode(), "Egreso registrado", "-$monto${comercio?.let { " · $it" } ?: ""}", "transaction_detail", txId)

    fun posibleDuplicado() =
        notificar(CanalesNotificacion.REVISION, 2002, "Posible movimiento duplicado", "Detectamos un gasto idéntico reciente. Tócalo para revisar.", "review")

    fun requiereVerificacionManual() =
        notificar(CanalesNotificacion.REVISION, 2001, "Revisión pendiente", "Hay un movimiento que no pudimos clasificar. Tócalo para revisarlo.", "review")

    fun guardadoEnColaOffline() =
        notificar(CanalesNotificacion.SINCRONIZACION, 3001, "Sin conexión", "Guardamos tu movimiento y lo procesaremos cuando vuelva internet.")

    fun sincronizacionCompletada(cantidad: Int) {
        if (cantidad <= 0) return
        notificar(CanalesNotificacion.SINCRONIZACION, 3002, "Sincronización completa", "Se procesaron $cantidad movimiento(s) pendiente(s).")
    }
}
