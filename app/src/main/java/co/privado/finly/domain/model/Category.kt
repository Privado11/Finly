package co.privado.finly.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val type: CategoryType,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
enum class CategoryType {
    @SerialName("income") income,
    @SerialName("expense") expense
}
