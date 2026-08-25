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

@Serializable
private data class CategoryPayload(
    @SerialName("user_id") val userId: String,
    val name: String,
    val type: CategoryType,
    val icon: String? = null,
    val color: String? = null
)

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> = flow { emit(getCategories().getOrThrow()) }

    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        supabase.from("categories").select().decodeList<Category>().sortedBy { it.name.lowercase() }
    }

    override suspend fun addCategory(name: String, type: CategoryType, icon: String?, color: String?): Result<Category> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Tu sesión expiró. Inicia sesión nuevamente.")
        supabase.from("categories").insert(CategoryPayload(userId, name.trim(), type, icon, color)) { select() }
            .decodeSingle<Category>()
    }

    override suspend fun deleteCategory(id: String): Result<Unit> = runCatching {
        supabase.from("categories").delete { filter { eq("id", id) } }
        Unit
    }
}
