package com.borayildirim.beautydate.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Visual transformation for Turkish phone numbers
 * Format: 0 (5--) --- -- --
 */
object PhoneNumberTransformation {
    
    /**
     * Safely transforms phone input into masked display: 0 (5--) --- -- --
     * Expects input to start with "0" followed by up to 10 digits
     */
    fun phoneMaskFilter(text: AnnotatedString): TransformedText {
        val input = text.text
        
        // If input is empty or just "0", show the placeholder format
        if (input.isEmpty() || input == "0") {
            val transformedText = "0 (___) ___ __ __"
            
            // Simple identity mapping for initial state
            val offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return when {
                        offset <= 0 -> 0
                        offset >= input.length -> transformedText.length
                        else -> 0
                    }
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return when {
                        offset <= 0 -> 0
                        offset >= transformedText.length -> input.length
                        else -> input.length.coerceAtMost(1)
                    }
                }
            }
            
            return TransformedText(
                AnnotatedString(transformedText),
                offsetMapping
            )
        }
        
        // Remove the leading "0" and get remaining digits (up to 10)
        val digits = if (input.startsWith("0")) {
            input.substring(1).filter { it.isDigit() }.take(10)
        } else {
            input.filter { it.isDigit() }.take(10)
        }

        val builder = StringBuilder()
        builder.append("0 (")

        // First 3 digits (area code)
        for (i in 0 until 3) {
            if (i < digits.length) {
                builder.append(digits[i])
            } else {
                builder.append("_")
            }
        }

        builder.append(") ")

        // Next 3 digits
        for (i in 3 until 6) {
            if (i < digits.length) {
                builder.append(digits[i])
            } else {
                builder.append("_")
            }
        }

        builder.append(" ")

        // Next 2 digits
        for (i in 6 until 8) {
            if (i < digits.length) {
                builder.append(digits[i])
            } else {
                builder.append("_")
            }
        }

        builder.append(" ")

        // Last 2 digits
        for (i in 8 until 10) {
            if (i < digits.length) {
                builder.append(digits[i])
            } else {
                builder.append("_")
            }
        }

        val transformedText = builder.toString()

        // Create safe OffsetMapping with proper bounds checking
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, input.length)
                
                // Handle the initial "0" prefix
                if (safeOffset == 0) return 0
                if (safeOffset == 1) return 3 // After "0 ("
                
                // Calculate position based on digit count (excluding the "0" prefix)
                val digitPosition = (safeOffset - 1).coerceIn(0, digits.length)
                return when {
                    digitPosition <= 3 -> 3 + digitPosition // "0 (" + digits
                    digitPosition <= 6 -> 5 + digitPosition // "0 (" + ") " + digits
                    digitPosition <= 8 -> 6 + digitPosition // "0 (" + ") " + " " + digits
                    else -> 7 + digitPosition // "0 (" + ") " + " " + " " + digits
                }.coerceAtMost(transformedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, transformedText.length)
                
                // Map positions back to original text
                return when {
                    safeOffset <= 3 -> 1 // In "0 (" area, map to after "0"
                    safeOffset <= 6 -> (safeOffset - 3).coerceAtMost(4) + 1 // First 3 digits area
                    safeOffset <= 7 -> 4 // In ") " area
                    safeOffset <= 10 -> (safeOffset - 7).coerceAtMost(3) + 4 // Second 3 digits area
                    safeOffset <= 11 -> 7 // In " " area
                    safeOffset <= 13 -> (safeOffset - 11).coerceAtMost(2) + 7 // Third 2 digits area
                    safeOffset <= 14 -> 9 // In " " area
                    else -> (safeOffset - 14).coerceAtMost(2) + 9 // Last 2 digits area
                }.coerceIn(0, input.length)
            }
        }

        return TransformedText(
            AnnotatedString(transformedText),
            offsetMapping
        )
    }
    
    /**
     * Visual transformation instance for phone number fields
     */
    val phoneTransformation = VisualTransformation { text ->
        phoneMaskFilter(text)
    }
}