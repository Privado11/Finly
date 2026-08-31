package co.privado.finly.domain.repository

import co.privado.finly.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    suspend fun getTransactions(): Result<List<Transaction>>
    suspend fun getTransactionById(id: String): Result<Transaction>
    suspend fun addTransaction(transaction: Transaction): Result<Transaction>
    suspend fun updateTransaction(id: String, transaction: Transaction): Result<Transaction>
    suspend fun deleteTransaction(id: String): Result<Unit>
    suspend fun existsDuplicate(amount: Double, sourceAccountId: String, windowMinutes: Int = 10): Boolean
    fun clearCache()
    val transactionUpdates: kotlinx.coroutines.flow.SharedFlow<Unit>
}
