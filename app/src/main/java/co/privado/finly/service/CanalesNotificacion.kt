package co.privado.finly.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object CanalesNotificacion {
    const val MOVIMIENTOS = "canal_movimientos"
    const val REVISION = "canal_revision"
    const val SINCRONIZACION = "canal_sincronizacion"

    fun crearCanales(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(MOVIMIENTOS, "Movimientos registrados", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notificaciones cuando se registra un ingreso, egreso o transferencia"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(REVISION, "Verificación manual", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Movimientos que requieren verificación manual"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(SINCRONIZACION, "Sincronización", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Estado de la cola offline y sincronización"
            }
        )
    }
}
