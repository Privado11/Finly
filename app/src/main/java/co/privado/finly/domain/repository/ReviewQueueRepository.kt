package co.privado.finly.domain.repository

import co.privado.finly.domain.model.ReviewQueueItem

interface ReviewQueueRepository {
    suspend fun getPending(): Result<List<ReviewQueueItem>>
    suspend fun add(item: ReviewQueueItem): Result<Unit>
    suspend fun markResolved(id: String): Result<Unit>
}
