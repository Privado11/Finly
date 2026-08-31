package co.privado.finly.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val type: AccountType,
    val currency: String = "COP",
    val active: Boolean = true,
    @SerialName("opening_balance") val openingBalance: Double = 0.0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class AccountBalance(
    @SerialName("account_id") val id: String,
    val name: String,
    val type: AccountType,
    val currency: String,
    val active: Boolean,
    @SerialName("opening_balance") val openingBalance: Double,
    val balance: Double
)

@Serializable
enum class AccountType {
    @SerialName("cash") cash,
    @SerialName("bank") bank,
    @SerialName("credit_card") credit_card,
    @SerialName("digital_wallet") digital_wallet
}
