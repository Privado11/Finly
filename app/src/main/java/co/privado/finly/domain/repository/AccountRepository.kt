package co.privado.finly.domain.repository

import co.privado.finly.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    suspend fun getAccounts(forceRefresh: Boolean = false): Result<List<Account>>
    suspend fun addAccount(name: String, type: co.privado.finly.domain.model.AccountType, currency: String = "COP"): Result<Account>
    suspend fun updateAccount(account: Account): Result<Account>
    suspend fun deleteAccount(id: String): Result<Unit>
    fun clearCache()
}
