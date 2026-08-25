package co.privado.finly.data.repository

import co.privado.finly.domain.model.Transaction
import co.privado.finly.domain.repository.TransactionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class TransactionPayload(
    @SerialName("user_id") val userId: String,
    @SerialName("source_account_id") val sourceAccountId: String,
    @SerialName("destination_account_id") val destinationAccountId: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    val type: co.privado.finly.domain.model.TransactionType,
    val amount: Double,
    val currency: String,
    val merchant: String? = null,
    val description: String? = null,
    val source: co.privado.finly.domain.model.TransactionSource,
    val date: String
)

@Singleton
class TransactionRepositoryImpl @Inject constructor(private val supabase: SupabaseClient) : TransactionRepository {
    override fun observeTransactions(): Flow<List<Transaction>> = flow { emit(getTransactions().getOrThrow()) }
    override suspend fun getTransactions(): Result<List<Transaction>> = runCatching {
        supabase.from("transactions").select().decodeList<Transaction>().sortedByDescending { it.date }
    }
    override suspend fun addTransaction(transaction: Transaction): Result<Transaction> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id ?: throw IllegalStateException("Tu sesión expiró. Inicia sesión nuevamente.")
        val payload = TransactionPayload(userId, transaction.sourceAccountId, transaction.destinationAccountId, transaction.categoryId, transaction.type, transaction.amount, transaction.currency, transaction.merchant, transaction.description, transaction.source, transaction.date)
        supabase.from("transactions").insert(payload) { select() }.decodeSingle<Transaction>()
    }
    override suspend fun deleteTransaction(id: String): Result<Unit> = runCatching { supabase.from("transactions").delete { filter { eq("id", id) } }; Unit }
    override suspend fun existsDuplicate(amount: Double, sourceAccountId: String, windowMinutes: Int): Boolean = runCatching {
        supabase.from("transactions").select { filter { eq("source_account_id", sourceAccountId); eq("amount", amount) } }.decodeList<Transaction>().isNotEmpty()
    }.getOrDefault(false)
}
