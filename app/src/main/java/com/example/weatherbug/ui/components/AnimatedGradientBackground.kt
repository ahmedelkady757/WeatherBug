package com.example.weatherbug.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun AnimatedGradientBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_anim")
    
    val color1 = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    val color2 = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
    val color3 = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
    val colorBg = MaterialTheme.colorScheme.background

    val offsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorBg) // Ensure there's a base color just in case
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val brush = Brush.linearGradient(
                colors = listOf(color1, color2, color3, color1),
                start = Offset(offsetAnim, offsetAnim),
                end = Offset(size.width - offsetAnim, size.height - offsetAnim)
            )
            drawRect(brush = brush, size = size)
        }
    }
}
