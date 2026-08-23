package co.privado.finly.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class NotificacionPendiente(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val texto: String,
    val capturadaEn: Long = System.currentTimeMillis()
)
