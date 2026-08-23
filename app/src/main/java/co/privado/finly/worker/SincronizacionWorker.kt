package co.privado.finly.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import co.privado.finly.data.local.ColaOfflineDataSource
import co.privado.finly.data.repository.ProcesadorNotificaciones
import co.privado.finly.service.NotificadorApp
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SincronizacionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val colaOffline: ColaOfflineDataSource,
    private val procesadorNotificaciones: ProcesadorNotificaciones,
    private val notificador: NotificadorApp
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pendientes = colaOffline.obtenerTodas()
        var exitosos = 0
        pendientes.forEach { p ->
            val exito = procesadorNotificaciones.procesar(p.packageName, p.texto)
            if (exito) {
                colaOffline.eliminar(p.id)
                exitosos++
            }
        }
        if (exitosos > 0) notificador.sincronizacionCompletada(exitosos)
        return Result.success()
    }

    companion object {
        fun programar(context: Context) {
            val request = OneTimeWorkRequestBuilder<SincronizacionWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "sincronizar_pendientes",
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
