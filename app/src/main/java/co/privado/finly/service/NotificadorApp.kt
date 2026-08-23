package co.privado.finly.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import co.privado.finly.MainActivity
import co.privado.finly.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificadorApp @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun intentBandeja(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destino", "review")
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun notificar(canal: String, id: Int, titulo: String, texto: String, withAction: Boolean = false) {
        val builder = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.mipmap.ic_launcher) // usa ícono de app por defecto; reemplazar por ic_notificacion cuando exista
            .setContentTitle(titulo)
            .setContentText(texto)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (withAction) builder.setContentIntent(intentBandeja())
        // POST_NOTIFICATIONS debe estar concedido en Android 13+
        try { NotificationManagerCompat.from(context).notify(id, builder.build()) } catch (_: SecurityException) {}
    }

    fun ingresoRegistrado(monto: String, comercio: String?) =
        notificar(CanalesNotificacion.MOVIMIENTOS, 1001, "Ingreso registrado", "+$monto${comercio?.let { " · $it" } ?: ""}")

    fun egresoRegistrado(monto: String, comercio: String?) =
        notificar(CanalesNotificacion.MOVIMIENTOS, 1002, "Egreso registrado", "-$monto${comercio?.let { " · $it" } ?: ""}")

    fun transferenciaRegistrada(monto: String) =
        notificar(CanalesNotificacion.MOVIMIENTOS, 1003, "Transferencia registrada", monto)

    fun requiereVerificacionManual() =
        notificar(CanalesNotificacion.REVISION, 2001, "Revisión pendiente", "Hay un movimiento que no pudimos clasificar. Tócalo para revisarlo.", withAction = true)

    fun guardadoEnColaOffline() =
        notificar(CanalesNotificacion.SINCRONIZACION, 3001, "Sin conexión", "Guardamos tu movimiento y lo procesaremos cuando vuelva internet.")

    fun sincronizacionCompletada(cantidad: Int) {
        if (cantidad <= 0) return
        notificar(CanalesNotificacion.SINCRONIZACION, 3002, "Sincronización completa", "Se procesaron $cantidad movimiento(s) pendiente(s).")
    }
}
