package co.privado.finly.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.ui.graphics.vector.ImageVector

fun String?.toIcon(): ImageVector {
    if (this.isNullOrBlank()) return Icons.Filled.Category
    return try {
        val clz = Class.forName("androidx.compose.material.icons.filled.${this}Kt")
        val method = clz.declaredMethods.first { it.name == "get$this" }
        method.invoke(null, Icons.Filled) as ImageVector
    } catch (e: Exception) {
        Icons.Filled.Category
    }
}
