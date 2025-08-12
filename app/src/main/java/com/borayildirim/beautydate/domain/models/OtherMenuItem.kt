package com.borayildirim.beautydate.domain.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Domain model for "Other" menu items
 * Represents menu items in the Other section
 */
data class OtherMenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String,
    val isSpecial: Boolean = false, // For logout button
    val isEnabled: Boolean = true
)

/**
 * Factory object for creating menu items
 * Centralizes menu item creation and maintains consistency
 */
object OtherMenuItemFactory {
    
    /**
     * Creates all menu items for the Other section
     * @return List of OtherMenuItem objects
     */
    fun createMenuItems(): List<OtherMenuItem> {
        return listOf(
            OtherMenuItem(
                id = "profile_settings",
                title = "Profil ve Ayarlar",
                icon = Icons.Default.Person,
                route = "profile_settings"
            ),
            OtherMenuItem(
                id = "finance",
                title = "Finans",
                icon = Icons.Default.AccountBalance,
                route = "finance",
                isEnabled = true // Finance system implemented
            ),
            OtherMenuItem(
                id = "statistics",
                title = "İstatistikler",
                icon = Icons.Default.BarChart,
                route = "statistics",
                isEnabled = true // Statistics system implemented
            ),
            OtherMenuItem(
                id = "how_to_use",
                title = "Nasıl Kullanılır?",
                icon = Icons.Default.Help,
                route = "tutorial",
                isEnabled = true // Tutorial system implemented
            ),
            OtherMenuItem(
                id = "notifications",
                title = "Bildirimler",
                icon = Icons.Default.Notifications,
                route = "notifications",
                isEnabled = false // Coming soon
            ),
            OtherMenuItem(
                id = "theme",
                title = "Tema",
                icon = Icons.Default.Palette,
                route = "theme"
            ),
            OtherMenuItem(
                id = "feedback",
                title = "Geri Bildirim",
                icon = Icons.Default.Feedback,
                route = "feedback"
            ),
            OtherMenuItem(
                id = "logout",
                title = "Çıkış Yap",
                icon = Icons.Default.ExitToApp,
                route = "logout",
                isSpecial = true
            )
        )
    }
} 