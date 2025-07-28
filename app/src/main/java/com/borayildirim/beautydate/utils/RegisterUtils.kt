package com.borayildirim.beautydate.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Utility object for registration-related helper functions
 */
object RegisterUtils {
    /**
     * Returns a VisualTransformation for Turkish phone numbers in the format 0 (5--) --- ----
     * Shows "0 (5--) --- ----" and fills dashes with user input
     */
    val phoneMaskTransformation = VisualTransformation { text ->
        val digits = text.text.filter { it.isDigit() }
        val out = buildString {
            append("0 (5")
            if (digits.isNotEmpty()) {
                append(digits.take(1))
                if (digits.length > 1) {
                    append(digits.substring(1, minOf(3, digits.length)))
                }
                repeat(3 - minOf(3, digits.length)) { append("-") }
            } else {
                append("--")
            }
            append(") ")
            if (digits.length > 3) {
                append(digits.substring(3, minOf(6, digits.length)))
                repeat(3 - minOf(3, digits.length - 3)) { append("-") }
            } else {
                append("---")
            }
            append(" ")
            if (digits.length > 6) {
                append(digits.substring(6, minOf(8, digits.length)))
                repeat(2 - minOf(2, digits.length - 6)) { append("-") }
            } else {
                append("--")
            }
            append(" ")
            if (digits.length > 8) {
                append(digits.substring(8, minOf(10, digits.length)))
                repeat(2 - minOf(2, digits.length - 8)) { append("-") }
            } else {
                append("--")
            }
        }
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 0 -> 0
                    offset <= 1 -> 3
                    offset <= 3 -> offset + 3
                    offset <= 6 -> offset + 4
                    offset <= 8 -> offset + 5
                    else -> offset + 6
                }
            }
            
            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 0 -> 0
                    offset <= 3 -> 1
                    offset <= 7 -> offset - 3
                    offset <= 11 -> offset - 4
                    offset <= 14 -> offset - 5
                    else -> offset - 6
                }
            }
        }
        
        TransformedText(AnnotatedString(out), offsetMapping)
    }
} 