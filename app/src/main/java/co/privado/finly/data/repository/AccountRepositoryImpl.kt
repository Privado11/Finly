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

@Serializable
private data class AccountPayload(
    @SerialName("user_id") val userId: String,
    val name: String,
    val type: AccountType,
    val currency: String
)

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AccountRepository {

    override fun observeAccounts(): Flow<List<Account>> = flow {
        emit(getAccounts().getOrThrow())
    }

    override suspend fun getAccounts(): Result<List<Account>> = runCatching {
        supabase.from("accounts").select().decodeList<Account>()
            .filter { it.active }
            .sortedBy { it.name.lowercase() }
    }

    override suspend fun addAccount(name: String, type: AccountType, currency: String): Result<Account> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Tu sesión expiró. Inicia sesión nuevamente.")
        supabase.from("accounts").insert(
            AccountPayload(userId = userId, name = name.trim(), type = type, currency = currency)
        ) { select() }.decodeSingle<Account>()
    }

    override suspend fun updateAccount(account: Account): Result<Account> = runCatching {
        supabase.from("accounts").update(
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
    }

    override suspend fun deleteAccount(id: String): Result<Unit> = runCatching {
        supabase.from("accounts").delete { filter { eq("id", id) } }
        Unit
    }
}
