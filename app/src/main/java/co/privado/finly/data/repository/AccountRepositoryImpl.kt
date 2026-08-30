package co.privado.finly.data.repository

import co.privado.finly.domain.model.Account
import co.privado.finly.domain.model.AccountType
import co.privado.finly.domain.repository.AccountRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton
import co.privado.finly.data.local.SessionDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Serializable
private data class AccountPayload(
    @SerialName("user_id") val userId: String,
    val name: String,
    val type: AccountType,
    val currency: String
)

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionStore: SessionDataStore
) : AccountRepository {

    private val _state = MutableStateFlow<List<Account>?>(null)

    override fun observeAccounts(): Flow<List<Account>> = _state.asStateFlow().map { it ?: emptyList() }

    override suspend fun getAccounts(forceRefresh: Boolean): Result<List<Account>> = runCatching {
        if (!forceRefresh && _state.value != null) {
            return@runCatching _state.value!!
        }
        val accounts = supabase.from("accounts").select().decodeList<Account>().sortedBy { it.name.lowercase() }
        _state.value = accounts
        accounts
    }

    override suspend fun addAccount(name: String, type: AccountType, currency: String): Result<Account> = runCatching {
        val userId = sessionStore.getUserId() ?: throw IllegalStateException("Tu sesión expiró. Inicia sesión nuevamente.")
        val acc = supabase.from("accounts").insert(
            AccountPayload(userId = userId, name = name.trim(), type = type, currency = currency)
        ) { select() }.decodeSingle<Account>()
        _state.value = null // Invalidate cache
        acc
    }

    override suspend fun updateAccount(account: Account): Result<Account> = runCatching {
        val acc = supabase.from("accounts").update(
            AccountPayload(
                userId = account.userId,
                name = account.name.trim(),
                type = account.type,
                currency = account.currency
            )
        ) {
            filter { eq("id", account.id) }
            select()
        }.decodeSingle<Account>()
        _state.value = null // Invalidate cache
        acc
    }

    override suspend fun deleteAccount(id: String): Result<Unit> = runCatching {
        supabase.from("accounts").delete { filter { eq("id", id) } }
        _state.value = null // Invalidate cache
        Unit
    }
}
