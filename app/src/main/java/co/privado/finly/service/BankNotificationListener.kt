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
import kotlinx.coroutines.cancel

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

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d(TAG, "Notificación recibida de: ${sbn.packageName}")
        scope.launch {
            // 1. Verificar whitelist — lee del caché local en disco, nunca de la red (§4.1)
            val permitido = try {
                sbn.packageName == "com.android.shell" || whitelistRepository.isAllowed(sbn.packageName)
            } catch (e: Exception) {
                Log.e(TAG, "Error verificando whitelist para ${sbn.packageName}: ${e.message}")
                false
            }

            if (!permitido) {
                Log.v(TAG, "App no está en whitelist: ${sbn.packageName}")
                return@launch
            }

            Log.d(TAG, "App permitida: ${sbn.packageName}")

            // 2. Extraer texto de la notificación de manera segura (evita ClassCastException con CharSequence)
            val extras = sbn.notification.extras
            val titulo = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
            val textoCorto = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
            val textoLargo = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim() ?: ""

            // Preferimos el texto largo si existe, si no, el corto. Y le pegamos el título que suele tener contexto útil.
            val cuerpo = textoLargo.ifBlank { textoCorto }
            val textoCombinado = listOf(titulo, cuerpo).filter { it.isNotBlank() }.joinToString("\n")

            if (textoCombinado.isBlank()) {
                Log.w(TAG, "Notificación sin texto útil de ${sbn.packageName}")
                return@launch
            }

            Log.d(TAG, "Texto capturado (${textoCombinado.length} chars): ${textoCombinado.take(100)}")

            // 3. Con internet: procesar inmediatamente. Sin internet: encolar para reintentar (§5.1)
            val finalPackageName = if (sbn.packageName == "com.android.shell") "com.bancolombia.app" else sbn.packageName
            if (conectividad.hayInternet()) {
                Log.d(TAG, "Con internet — procesando directo")
                procesadorNotificaciones.procesar(finalPackageName, textoCombinado)
            } else {
                Log.d(TAG, "Sin internet — encolando offline")
                colaOffline.encolar(finalPackageName, textoCombinado)
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
