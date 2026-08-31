import java.text.NumberFormat
import java.util.Locale

fun formatAmount(amount: String): String {
    val cleanString = amount.replace(Regex("[^\\d]"), "")
    if (cleanString.isEmpty()) return ""
    val parsed = cleanString.toDouble()
    val formatter = NumberFormat.getNumberInstance(Locale("es", "CO"))
    return formatter.format(parsed)
}
