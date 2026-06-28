package com.bvue.ui.anim

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A moving highlight sweep, for skeleton loaders. Drives one [rememberInfiniteTransition] and draws in
 * the draw phase (drawWithCache). Use ONLY inside transient (loading) trees so it stops when removed.
 */
fun Modifier.shimmer(highlightColor: Color): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    drawWithCache {
        val w = size.width
        val band = w * 0.55f
        val x = -band + (w + band) * progress
        val brush = Brush.linearGradient(
            colors = listOf(Color.Transparent, highlightColor, Color.Transparent),
            start = Offset(x, 0f),
            end = Offset(x + band, size.height),
        )
        onDrawWithContent {
            drawContent()
            drawRect(brush)
        }
    }
}
