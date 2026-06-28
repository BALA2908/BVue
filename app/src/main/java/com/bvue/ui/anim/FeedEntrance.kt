package com.bvue.ui.anim

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Per-screen set of feed keys that have already animated in, so entrance plays only on first appearance. */
@Composable
fun rememberPlayedFeedKeys(): MutableMap<String, Boolean> = remember { mutableStateMapOf<String, Boolean>() }

/**
 * Fade + rise entrance for a feed item, animated via graphicsLayer (draw phase — no relayout). Guarded
 * by [played] keyed on a stable id so it fires once per item, never again on scroll-back or recompose.
 */
fun Modifier.feedEntrance(key: String, index: Int, played: MutableMap<String, Boolean>): Modifier = composed {
    val already = played[key] == true
    var shown by remember(key) { mutableStateOf(already) }
    LaunchedEffect(key) {
        if (!already) {
            delay((index.coerceAtMost(8) * 45).toLong())
            shown = true
            played[key] = true
        }
    }
    val progress by animateFloatAsState(if (shown) 1f else 0f, tween(300), label = "feedEntrance")
    graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * 24.dp.toPx()
    }
}
