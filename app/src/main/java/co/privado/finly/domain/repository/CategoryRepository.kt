package co.privado.finly.domain.repository

import co.privado.finly.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun addCategory(category: Category): Result<Category>
    suspend fun deleteCategory(id: String): Result<Unit>
}
