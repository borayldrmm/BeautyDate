package com.borayildirim.beautydate.utils

import android.content.Context
import android.widget.Toast

/**
 * Memory efficient Toast utility class
 * Singleton pattern to prevent multiple Toast instances
 * Automatically cancels previous toast to avoid memory leaks
 */
object ToastUtils {
    private var currentToast: Toast? = null
    
    /**
     * Shows success toast message
     * Cancels previous toast to prevent memory accumulation
     */
    fun showSuccess(context: Context, message: String) {
        showToast(context, message, Toast.LENGTH_SHORT)
    }
    
    /**
     * Shows error toast message
     * Cancels previous toast to prevent memory accumulation
     */
    fun showError(context: Context, message: String) {
        showToast(context, message, Toast.LENGTH_LONG)
    }
    
    /**
     * Shows info toast message
     * Cancels previous toast to prevent memory accumulation
     */
    fun showInfo(context: Context, message: String) {
        showToast(context, message, Toast.LENGTH_SHORT)
    }
    
    /**
     * Private method to handle toast creation and memory management
     */
    private fun showToast(context: Context, message: String, duration: Int) {
        // Cancel previous toast to prevent memory leaks
        currentToast?.cancel()
        
        // Create new toast and store reference
        currentToast = Toast.makeText(context, message, duration)
        currentToast?.show()
    }
    
    /**
     * Cancels current toast - useful for cleanup
     */
    fun cancel() {
        currentToast?.cancel()
        currentToast = null
    }
} 