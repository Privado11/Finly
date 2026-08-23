package co.privado.finly.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllowedApp(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("package_name") val packageName: String,
    @SerialName("display_name") val displayName: String? = null,
    val active: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)
