package co.privado.finly.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
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

private const val TAG = "BankNotifListener"

/**
 * §5 Frontend — NotificationListenerService reactivo (sin polling).
 *
 * Lee la whitelist del caché local persistido en DataStore (§4.1) — nunca toca la red.
 * Decide: procesar directo (con internet) vs encolar offline (sin internet).
 *
 * La gestión de sesión de Supabase queda a cargo de ProcesadorNotificaciones/AuthRepository;
 * el listener no necesita conocer SupabaseClient directamente.
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
        Log.d(TAG, "Notificación recibida de: ${sbn.packageName}")
        scope.launch {
            // 1. Verificar whitelist — lee del caché local en disco, nunca de la red (§4.1)
            val permitido = try {
                whitelistRepository.isAllowed(sbn.packageName)
            } catch (e: Exception) {
                Log.e(TAG, "Error verificando whitelist para ${sbn.packageName}: ${e.message}")
                // Si falla la lectura del DataStore encola como fallback seguro
                true
            }

            if (!permitido) {
                Log.v(TAG, "App no está en whitelist: ${sbn.packageName}")
                return@launch
            }

            Log.d(TAG, "App permitida: ${sbn.packageName}")

            // 2. Extraer texto de la notificación
            val texto = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
                ?: sbn.notification.extras.getString(Notification.EXTRA_BIG_TEXT)
                ?: run {
                    Log.w(TAG, "Notificación sin texto útil de ${sbn.packageName}")
                    return@launch
                }

            if (texto.isBlank()) {
                Log.w(TAG, "Texto de notificación vacío de ${sbn.packageName}")
                return@launch
            }

            Log.d(TAG, "Texto capturado (${texto.length} chars): ${texto.take(100)}")

            // 3. Con internet: procesar inmediatamente. Sin internet: encolar para reintentar (§5.1)
            if (conectividad.hayInternet()) {
                Log.d(TAG, "Con internet — procesando directo")
                procesadorNotificaciones.procesar(sbn.packageName, texto)
            } else {
                Log.d(TAG, "Sin internet — encolando offline")
                colaOffline.encolar(sbn.packageName, texto)
                notificador.guardadoEnColaOffline()
                SincronizacionWorker.programar(applicationContext)
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "NotificationListenerService CONECTADO — escuchando notificaciones")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "NotificationListenerService DESCONECTADO")
    }
}
