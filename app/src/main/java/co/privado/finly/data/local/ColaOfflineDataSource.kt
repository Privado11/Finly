package co.privado.finly.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import co.privado.finly.domain.model.NotificacionPendiente
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.offlineQueueStore by preferencesDataStore(name = "finly_offline_queue")

@Singleton
class ColaOfflineDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val keyQueue = stringPreferencesKey("pending_queue_json")

    suspend fun encolar(packageName: String, texto: String) {
        val pendiente = NotificacionPendiente(packageName = packageName, texto = texto)
        val actuales = obtenerTodas().toMutableList()
        actuales.add(pendiente)
        persistir(actuales)
    }

    suspend fun obtenerTodas(): List<NotificacionPendiente> {
        val raw = context.offlineQueueStore.data.map { it[keyQueue] ?: "[]" }.first()
        return try { json.decodeFromString(raw) } catch (_: Exception) { emptyList() }
    }

    suspend fun eliminar(id: String) {
        val filtradas = obtenerTodas().filterNot { it.id == id }
        persistir(filtradas)
    }

    suspend fun limpiar() {
        persistir(emptyList())
    }

    suspend fun tamanio(): Int = obtenerTodas().size

    private suspend fun persistir(lista: List<NotificacionPendiente>) {
        val raw = json.encodeToString(lista)
        context.offlineQueueStore.edit { it[keyQueue] = raw }
    }
}
