package co.privado.finly.data.repository

import android.util.Log
import co.privado.finly.domain.model.ParseData
import co.privado.finly.domain.model.ParseNotificationResponse
import co.privado.finly.domain.model.ReviewQueueItem
import co.privado.finly.domain.model.Transaction
import co.privado.finly.domain.model.TransactionSource
import co.privado.finly.domain.model.TransactionType
import co.privado.finly.domain.repository.AccountRepository
import co.privado.finly.domain.repository.TransactionRepository
import co.privado.finly.service.NotificadorApp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.ktor.client.call.body
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ProcesadorNotif"

/**
 * Orquesta la cadena: Edge Function parse-notification → validación → guardado en Supabase o review_queue.
 *
 * Bugs corregidos:
 * - supabase.functions.invoke() retorna HttpResponse, no el DTO directamente — se deserializa el body
 * - Ahora SÍ se guarda la transacción en Supabase tras parseo exitoso
 * - Se usa la primera cuenta activa del usuario como source_account por defecto
 */
@Singleton
class ProcesadorNotificaciones @Inject constructor(
    private val supabase: SupabaseClient,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val notificador: NotificadorApp
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun procesar(packageName: String, texto: String): Boolean {
        return try {
            Log.d(TAG, "Procesando notificación de $packageName: ${texto.take(80)}...")

            val body = buildJsonObject {
                put("package_name", packageName)
                put("text", texto)
            }

            // Llama a la Edge Function (requiere JWT válido — supabase.auth lo inyecta)
            val httpResponse = supabase.functions.invoke("parse-notification", body)
            val responseBody: String = httpResponse.body()
            Log.d(TAG, "Respuesta Edge Function: $responseBody")

            val response = try {
                json.decodeFromString<ParseNotificationResponse>(responseBody)
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo deserializar respuesta: ${e.message}")
                return manejarFallback(packageName, texto, "invalid_json")
            }

            if (!response.ok || response.data == null) {
                Log.w(TAG, "Edge Function respondió ok=false: ${response.reason}")
                return manejarFallback(packageName, texto, response.reason ?: "low_confidence")
            }

            val data = response.data
            // Validación obligatoria §5.4 backend
            if (data.confidence == "low") return manejarFallback(packageName, texto, "low_confidence")
            if (data.type !in setOf("income", "expense", "transfer")) return manejarFallback(packageName, texto, "invalid_type")
            val amount = data.amount ?: return manejarFallback(packageName, texto, "missing_amount")
            if (amount < 0) return manejarFallback(packageName, texto, "negative_amount")

            // Obtener la primera cuenta activa como source account
            val accounts = accountRepository.getAccounts().getOrDefault(emptyList())
            val sourceAccount = accounts.firstOrNull()
            if (sourceAccount == null) {
                Log.w(TAG, "No hay cuentas registradas — no se puede guardar la transacción")
                return manejarFallback(packageName, texto, "no_accounts")
            }

            // Determinar el tipo de transacción
            val transactionType = when (data.type) {
                "income" -> TransactionType.income
                "expense" -> TransactionType.expense
                "transfer" -> TransactionType.transfer
                else -> TransactionType.expense
            }

            // Deduplicación: verificar si ya existe una transacción manual similar
            val isDuplicate = transactionRepository.existsDuplicate(amount, sourceAccount.id!!)
            if (isDuplicate) {
                Log.d(TAG, "Posible duplicado detectado, descartando")
                return true // No es un error, simplemente ya existe
            }

            // GUARDAR la transacción en Supabase
            val transaction = Transaction(
                sourceAccountId = sourceAccount.id,
                type = transactionType,
                amount = amount,
                currency = data.currency,
                merchant = data.merchant,
                description = "Captura automática",
                source = TransactionSource.notification_llm,
                rawNotification = texto,
                date = Instant.now().toString()
            )

            val result = transactionRepository.addTransaction(transaction)
            result.onSuccess {
                Log.d(TAG, "Transacción guardada exitosamente: ${it.id}")
                when (data.type) {
                    "income" -> notificador.ingresoRegistrado(amount.toString(), data.merchant)
                    "expense" -> notificador.egresoRegistrado(amount.toString(), data.merchant)
                    "transfer" -> notificador.transferenciaRegistrada(amount.toString())
                }
            }.onFailure {
                Log.e(TAG, "Error guardando transacción: ${it.message}")
                return manejarFallback(packageName, texto, "save_failed: ${it.message}")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando notificación: ${e.message}", e)
            manejarFallback(packageName, texto, e.message ?: "llm_error")
            false
        }
    }

    private suspend fun manejarFallback(packageName: String, texto: String, reason: String): Boolean {
        Log.w(TAG, "Fallback → review_queue: $reason")
        try {
            supabase.from("review_queue").insert(
                ReviewQueueItem(packageName = packageName, originalText = texto, failureReason = reason)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error insertando en review_queue: ${e.message}")
        }
        notificador.requiereVerificacionManual()
        return false
    }
}
