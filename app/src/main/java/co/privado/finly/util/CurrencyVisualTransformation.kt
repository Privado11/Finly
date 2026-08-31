package co.privado.finly.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        
        val cleanText = originalText.replace(Regex("[^\\d]"), "")
        if (cleanText.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        
        val number = cleanText.toLongOrNull() ?: 0L
        val formatter = NumberFormat.getNumberInstance(Locale("es", "CO")) as DecimalFormat
        formatter.applyPattern("#,###")
        val formatted = formatter.format(number).replace(',', '.')
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = 0
                var originalCount = 0
                for (i in formatted.indices) {
                    if (originalCount == offset) break
                    if (formatted[i].isDigit()) {
                        originalCount++
                    }
                    transformedOffset++
                }
                return transformedOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalOffset = 0
                for (i in 0 until offset) {
                    if (i < formatted.length && formatted[i].isDigit()) {
                        originalOffset++
                    }
                }
                return originalOffset
            }
        }
        
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
