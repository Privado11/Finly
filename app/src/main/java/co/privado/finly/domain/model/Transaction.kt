package co.privado.finly.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("source_account_id") val sourceAccountId: String,
    @SerialName("destination_account_id") val destinationAccountId: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    val type: TransactionType,
    val amount: Double,
    val currency: String = "COP",
    val merchant: String? = null,
    val description: String? = null,
    val source: TransactionSource = TransactionSource.manual,
    @SerialName("raw_notification") val rawNotification: String? = null,
    val date: String, // ISO-8601 timestamptz
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
enum class TransactionType {
    @SerialName("income") income,
    @SerialName("expense") expense,
    @SerialName("transfer") transfer
}

@Serializable
enum class TransactionSource {
    @SerialName("manual") manual,
    @SerialName("notification_regex") notification_regex,
    @SerialName("notification_llm") notification_llm
}
