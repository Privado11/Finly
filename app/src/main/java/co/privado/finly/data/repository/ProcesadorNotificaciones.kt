package co.privado.finly.data.repository

import co.privado.finly.domain.model.ParseNotificationResponse
import co.privado.finly.domain.model.ReviewQueueItem
import co.privado.finly.domain.repository.ReviewQueueRepository
import co.privado.finly.service.NotificadorApp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orquesta la cadena: Edge Function parse-notification -> validación -> guardado en Supabase o review_queue
 * Sucesor del pseudocódigo del backend 5.2.
 */
@Singleton
class ProcesadorNotificaciones @Inject constructor(
    private val supabase: SupabaseClient,
    private val notificador: NotificadorApp
) {
    suspend fun procesar(packageName: String, texto: String): Boolean {
        return try {
            val body = buildJsonObject {
                put("package_name", packageName)
                put("text", texto)
            }
            // Llama a la Edge Function (requiere JWT válido — supabase.auth lo inyecta)
            val response = supabase.functions.invoke("parse-notification", body) as? ParseNotificationResponse
                ?: return manejarFallback(packageName, texto, "invalid_response")

            if (!response.ok || response.data == null) {
                return manejarFallback(packageName, texto, response.reason ?: "low_confidence")
            }

            val data = response.data
            // Validación obligatoria §5.4 backend
            if (data.confidence == "low") return manejarFallback(packageName, texto, "low_confidence")
            if (data.type !in setOf("income", "expense", "transfer")) return manejarFallback(packageName, texto, "invalid_type")
            val amount = data.amount ?: return manejarFallback(packageName, texto, "missing_amount")
            if (amount < 0) return manejarFallback(packageName, texto, "negative_amount")

            // TODO: deduplicación §5.5 — verificar transacción manual reciente con mismo monto/cuenta antes de insertar
            // TODO: resolución de suggestedDestinationAccount contra accounts existentes para transferencias

            // Guardado real en transactions se hace desde el caller con los IDs de cuenta/categoría resueltos.
            // Este método valida y notifica éxito parcial; el inset final ocurre en TransactionRepository.
            when (data.type) {
                "income" -> notificador.ingresoRegistrado(amount.toString(), data.merchant)
                "expense" -> notificador.egresoRegistrado(amount.toString(), data.merchant)
                "transfer" -> notificador.transferenciaRegistrada(amount.toString())
            }
            true
        } catch (e: Exception) {
            manejarFallback(packageName, texto, e.message ?: "llm_error")
            false
        }
    }

    private suspend fun manejarFallback(packageName: String, texto: String, reason: String): Boolean {
        // Inserta en review_queue para revisión manual
        try {
            supabase.from("review_queue").insert(
                ReviewQueueItem(packageName = packageName, originalText = texto, failureReason = reason)
            )
        } catch (_: Exception) {}
        notificador.requiereVerificacionManual()
        return false
    }
}
