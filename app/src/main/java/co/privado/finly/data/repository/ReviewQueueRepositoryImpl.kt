package co.privado.finly.data.repository

import co.privado.finly.data.local.SessionDataStore
import co.privado.finly.domain.model.ReviewQueueItem
import co.privado.finly.domain.repository.ReviewQueueRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewQueueRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionDataStore: SessionDataStore
) : ReviewQueueRepository {
    override suspend fun getPending(): Result<List<ReviewQueueItem>> = runCatching {
        val uid = sessionDataStore.getUserId() ?: throw Exception("No user logged in")
        supabase.postgrest["review_queue"]
            .select(Columns.ALL) {
                filter {
                    eq("user_id", uid)
                    eq("resolved", false)
                }
            }.decodeList<ReviewQueueItem>()
    }

    override suspend fun add(item: ReviewQueueItem): Result<Unit> = runCatching {
        val uid = sessionDataStore.getUserId() ?: throw Exception("No user logged in")
        supabase.postgrest["review_queue"].insert(item.copy(userId = uid))
    }

    override suspend fun markResolved(id: String): Result<Unit> = runCatching {
        supabase.postgrest["review_queue"]
            .update({
                set("resolved", true)
            }) {
                filter { eq("id", id) }
            }
    }
}
