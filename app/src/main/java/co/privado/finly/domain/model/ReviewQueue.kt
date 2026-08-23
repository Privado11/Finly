package co.privado.finly.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewQueueItem(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("package_name") val packageName: String,
    @SerialName("original_text") val originalText: String,
    @SerialName("failure_reason") val failureReason: String? = null,
    val resolved: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)
