package co.privado.finly.domain.repository

import co.privado.finly.domain.model.Account
import co.privado.finly.domain.model.AccountBalance
import co.privado.finly.domain.model.AccountType
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<AccountBalance>>
    suspend fun getAccounts(forceRefresh: Boolean = false): Result<List<AccountBalance>>
    suspend fun addAccount(name: String, type: AccountType, openingBalance: Double = 0.0, currency: String = "COP"): Result<Account>
    suspend fun updateAccount(id: String, name: String, type: AccountType, openingBalance: Double, currency: String = "COP"): Result<Account>
    suspend fun deleteAccount(id: String): Result<Unit>
    fun clearCache()
}
