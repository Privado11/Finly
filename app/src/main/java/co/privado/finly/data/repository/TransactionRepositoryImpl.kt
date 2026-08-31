package co.privado.finly.data.repository

import co.privado.finly.data.local.SessionDataStore
import co.privado.finly.domain.model.Transaction
import co.privado.finly.domain.repository.TransactionRepository
import io.github.jan.supabase.SupabaseClient
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
    @SerialName("raw_notification") val rawNotification: String? = null,
    val date: String
)

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionStore: SessionDataStore,
    private val accountRepository: co.privado.finly.domain.repository.AccountRepository
) : TransactionRepository {

    private val _transactionUpdates = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST)
    override val transactionUpdates: kotlinx.coroutines.flow.SharedFlow<Unit> = _transactionUpdates

    private fun notifyUpdate() { _transactionUpdates.tryEmit(Unit) }

    override fun observeTransactions(): Flow<List<Transaction>> = flow { emit(getTransactions().getOrThrow()) }
    override suspend fun getTransactions(): Result<List<Transaction>> = runCatching {
        supabase.from("transactions").select().decodeList<Transaction>().sortedByDescending { it.date }
    }
    override suspend fun getTransactionById(id: String): Result<Transaction> = runCatching {
        supabase.from("transactions").select { filter { eq("id", id) } }.decodeSingle<Transaction>()
    }
    override suspend fun addTransaction(transaction: Transaction): Result<Transaction> = runCatching {
        val userId = sessionStore.getUserId() ?: throw java.lang.IllegalStateException("Tu sesión expiró. Inicia sesión nuevamente.")
        val payload = TransactionPayload(userId, transaction.sourceAccountId, transaction.destinationAccountId, transaction.categoryId, transaction.type, transaction.amount, transaction.currency, transaction.merchant, transaction.description, transaction.source, transaction.rawNotification, transaction.date)
        val result = supabase.from("transactions").insert(payload) { select() }.decodeSingle<Transaction>()
        accountRepository.getAccounts(forceRefresh = true)
        notifyUpdate()
        result
    }
    override suspend fun updateTransaction(id: String, transaction: Transaction): Result<Transaction> = runCatching {
        val userId = sessionStore.getUserId() ?: throw java.lang.IllegalStateException("Tu sesión expiró. Inicia sesión nuevamente.")
        val payload = TransactionPayload(userId, transaction.sourceAccountId, transaction.destinationAccountId, transaction.categoryId, transaction.type, transaction.amount, transaction.currency, transaction.merchant, transaction.description, transaction.source, transaction.rawNotification, transaction.date)
        val result = supabase.from("transactions").update(payload) {
            filter { eq("id", id) }
            select()
        }.decodeSingle<Transaction>()
        accountRepository.getAccounts(forceRefresh = true)
        notifyUpdate()
        result
    }
    override suspend fun deleteTransaction(id: String): Result<Unit> = runCatching { 
        supabase.from("transactions").delete { filter { eq("id", id) } }
        accountRepository.getAccounts(forceRefresh = true)
        notifyUpdate()
        Unit 
    }
    override suspend fun existsDuplicate(amount: Double, sourceAccountId: String, windowMinutes: Int): Boolean = runCatching {
        val cutoffTime = java.time.Instant.now().minusSeconds((windowMinutes * 60).toLong()).toString()
        supabase.from("transactions").select { 
            filter { 
                eq("source_account_id", sourceAccountId)
                eq("amount", amount)
                gte("date", cutoffTime)
            } 
        }.decodeList<Transaction>().isNotEmpty()
    }.getOrDefault(false)

    override fun clearCache() {
        // No local cache to clear in this implementation
    }
}
