package co.privado.finly.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParseNotificationResponse(
    val ok: Boolean,
    val data: ParseData? = null,
    val reason: String? = null,
    @SerialName("original_text") val originalText: String? = null
)

@Serializable
data class ParseData(
    val type: String?, // income | expense | null
    val amount: Double? = null,
    val currency: String = "COP",
    val merchant: String? = null,
    @SerialName("suggested_destination_account") val suggestedDestinationAccount: String? = null,
    val confidence: String // high | medium | low
)
