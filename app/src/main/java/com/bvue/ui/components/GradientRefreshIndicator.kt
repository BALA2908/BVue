package com.bvue.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.bvue.ui.anim.bvueSweep

/**
 * BVue-gradient pull-to-refresh ring. Tracks the pull (scale/alpha/rotation from distanceFraction) and
 * spins continuously while refreshing. The spin coroutine is cancelled the moment refreshing ends.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
) {
    val sweep = remember { bvueSweep() }
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            while (true) {
                rotation.animateTo(rotation.value + 360f, tween(900, easing = LinearEasing))
            }
        } else {
            rotation.snapTo(0f)
        }
    }
    val fraction = state.distanceFraction
    if (!isRefreshing && fraction <= 0.01f) return
    Box(
        modifier
            .padding(top = 14.dp)
            .size(38.dp)
            .graphicsLayer {
                val s = if (isRefreshing) 1f else fraction.coerceIn(0f, 1f)
                scaleX = s
                scaleY = s
                alpha = s
                rotationZ = if (isRefreshing) rotation.value else fraction * 220f
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawArc(
                brush = sweep,
                startAngle = 0f,
                sweepAngle = 300f,
                useCenter = false,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}
