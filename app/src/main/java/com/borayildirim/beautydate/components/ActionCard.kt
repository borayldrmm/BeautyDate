package com.borayildirim.beautydate.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Action card component for dashboard operations
 * Material3 compliant design with smooth animations
 * Memory efficient: minimized state and recomposition
 */
@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Animation states for press feedback
    var isPressed by remember { mutableStateOf(false) }
    
    // Memory efficient: animated values with controlled duration
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "card_scale"
    )
    
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 200),
        label = "background_color"
    )
    
    val animatedContentColor by animateColorAsState(
        targetValue = if (enabled) contentColor else contentColor.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 200),
        label = "content_color"
    )
    
    // Memory efficient: reused interaction source
    val interactionSource = remember { MutableInteractionSource() }
    
    Card(
        modifier = modifier
            .aspectRatio(1f) // Square cards for consistent grid layout
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Use Material3 default indication
                enabled = enabled,
                onClick = {
                    if (enabled) {
                        onClick()
                    }
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled) 6.dp else 2.dp,
            pressedElevation = if (enabled) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = animatedBackgroundColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon with proper sizing for touch targets
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(32.dp),
                    tint = animatedContentColor
                )
                
                // Title text with proper overflow handling
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = animatedContentColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    // Press state handling for animation feedback
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is androidx.compose.foundation.interaction.PressInteraction.Press -> {
                    isPressed = true
                }
                is androidx.compose.foundation.interaction.PressInteraction.Release,
                is androidx.compose.foundation.interaction.PressInteraction.Cancel -> {
                    isPressed = false
                }
            }
        }
    }
} 