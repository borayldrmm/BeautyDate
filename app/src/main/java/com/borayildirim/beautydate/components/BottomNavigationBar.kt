package com.borayildirim.beautydate.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.borayildirim.beautydate.navigation.BottomNavigationItem
import com.borayildirim.beautydate.navigation.bottomNavigationItems

/**
 * Modern Material Design 3 Bottom navigation bar component
 * Features dynamic oval indicators and smooth animations
 * Displays 5 tabs with "Diğer" as the rightmost tab with three dots icon
 */
@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.height(80.dp),
        tonalElevation = 8.dp
    ) {
        bottomNavigationItems.forEach { item ->
            val isSelected = currentRoute == item.route
            
            // Animated colors
            val iconColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(durationMillis = 300),
                label = "IconColor"
            )
            
            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(durationMillis = 300),
                label = "TextColor"
            )
            
            // Animated scale for selection effect with spring animation
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1.0f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                label = "IconScale"
            )
            
            // Animated background color with enhanced primary container
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                } else {
                    Color.Transparent
                },
                animationSpec = tween(durationMillis = 400),
                label = "BackgroundColor"
            )
            
            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(color = backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.scale(iconScale),
                            tint = iconColor
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = textColor
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the current graph
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Transparent, // We handle colors manually
                    selectedTextColor = Color.Transparent,
                    unselectedIconColor = Color.Transparent,
                    unselectedTextColor = Color.Transparent,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
} 