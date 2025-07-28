package com.borayildirim.beautydate.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Business
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing bottom navigation items
 * Follows Single Responsibility Principle
 * Updated with İşlemler (Operations) tab for quick business actions
 */
sealed class BottomNavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Randevular : BottomNavigationItem(
        route = "randevular",
        title = "Takvim",
        icon = Icons.Default.CalendarToday
    )
    
    object Ziyaretler : BottomNavigationItem(
        route = "ziyaretler", 
        title = "Randevular",
        icon = Icons.Default.Business
    )
    
    // Updated: More professional icon for business operations
    object Operations : BottomNavigationItem(
        route = "operations",
        title = "İşlemler", 
        icon = Icons.Default.Dashboard // Changed from FlashOn to Dashboard for more corporate look
    )
    
    object Musteriler : BottomNavigationItem(
        route = "musteriler",
        title = "Müşteriler",
        icon = Icons.Default.Group
    )
    
    object Diger : BottomNavigationItem(
        route = "diger",
        title = "Diğer",
        icon = Icons.Default.MoreHoriz
    )
}

/**
 * List of all bottom navigation items
 * Memory efficient: pre-computed list to avoid repeated object creation
 */
val bottomNavigationItems = listOf(
    BottomNavigationItem.Randevular,
    BottomNavigationItem.Ziyaretler,
    BottomNavigationItem.Operations,
    BottomNavigationItem.Musteriler,
    BottomNavigationItem.Diger
) 