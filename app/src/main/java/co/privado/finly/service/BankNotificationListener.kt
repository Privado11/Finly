package co.privado.finly.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import co.privado.finly.data.local.ColaOfflineDataSource
import co.privado.finly.data.repository.ProcesadorNotificaciones
import co.privado.finly.domain.repository.WhitelistRepository
import co.privado.finly.util.ConectividadHelper
import co.privado.finly.worker.SincronizacionWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * §5 Frontend — NotificationListenerService reactivo (sin polling).
 * Lee whitelist en cada evento (no cacheada) y decide: procesar directo vs encolar offline.
 */
@AndroidEntryPoint
class BankNotificationListener : NotificationListenerService() {

    @Inject lateinit var whitelistRepository: WhitelistRepository
    @Inject lateinit var procesadorNotificaciones: ProcesadorNotificaciones
    @Inject lateinit var conectividad: ConectividadHelper
    @Inject lateinit var colaOffline: ColaOfflineDataSource
    @Inject lateinit var notificador: NotificadorApp

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        scope.launch {
            val permitido = try { whitelistRepository.isAllowed(sbn.packageName) } catch (_: Exception) { false }
            if (!permitido) return@launch

            val texto = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
                ?: sbn.notification.extras.getString(Notification.EXTRA_BIG_TEXT)
                ?: return@launch

            if (texto.isBlank()) return@launch

            if (conectividad.hayInternet()) {
                procesadorNotificaciones.procesar(sbn.packageName, texto)
            } else {
                colaOffline.encolar(sbn.packageName, texto)
                notificador.guardadoEnColaOffline()
                SincronizacionWorker.programar(applicationContext)
            }
        }
    }
}
