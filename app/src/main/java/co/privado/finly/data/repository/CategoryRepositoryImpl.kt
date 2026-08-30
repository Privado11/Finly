package co.privado.finly.data.repository

import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.CategoryType
import co.privado.finly.domain.repository.CategoryRepository
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
private data class CategoryPayload(
    @SerialName("user_id") val userId: String,
    val name: String,
    val icon: String?,
    val color: String?,
    val type: CategoryType
)


@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionStore: SessionDataStore
) : CategoryRepository {

    private val _state = MutableStateFlow<List<Category>?>(null)

    override fun observeCategories(): Flow<List<Category>> = _state.asStateFlow().map { it ?: emptyList() }

    override suspend fun getCategories(forceRefresh: Boolean): Result<List<Category>> = runCatching {
        if (!forceRefresh && _state.value != null) {
            return@runCatching _state.value!!
        }
        val categories = supabase.from("categories").select().decodeList<Category>().sortedBy { it.name }
        _state.value = categories
        categories
    }

    override suspend fun addCategory(name: String, type: CategoryType, icon: String?, color: String?): Result<Category> = runCatching {
        val userId = sessionStore.getUserId() ?: throw IllegalStateException("Tu sesión expiró. Inicia sesión nuevamente.")
        val payload = CategoryPayload(userId, name.trim(), icon, color, type)
        val newCat = supabase.from("categories").insert(payload) { select() }.decodeSingle<Category>()
        _state.value = null // Invalidate cache
        newCat
    }

    override suspend fun deleteCategory(id: String): Result<Unit> = runCatching {
        supabase.from("categories").delete { filter { eq("id", id) } }
        _state.value = null // Invalidate cache
        Unit
    }
}
