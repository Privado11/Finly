package co.privado.finly.domain.repository

import co.privado.finly.domain.model.Category
import co.privado.finly.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun getCategories(forceRefresh: Boolean = false): Result<List<Category>>
    suspend fun addCategory(name: String, type: CategoryType, icon: String? = null, color: String? = null): Result<Category>
    suspend fun deleteCategory(id: String): Result<Unit>
}
