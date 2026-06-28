package com.bvue.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bvue.ui.anim.Motion

/**
 * Crossfades between an inactive (outlined) and active (filled) icon, with a one-shot scale bounce
 * fired only on the inactive→active edge (never on first composition). Used for Save + Shorts like.
 */
@Composable
fun AnimatedToggleIcon(
    active: Boolean,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    activeTint: Color,
    inactiveTint: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    val scale = remember { Animatable(1f) }
    var initialized by remember { mutableStateOf(false) }
    LaunchedEffect(active) {
        if (!initialized) {
            initialized = true
            return@LaunchedEffect
        }
        if (active) {
            scale.animateTo(1.35f, tween(120))
            scale.animateTo(1f, Motion.springBouncy())
        }
    }
    Crossfade(targetState = active, label = "toggleIcon") { isActive ->
        Icon(
            imageVector = if (isActive) activeIcon else inactiveIcon,
            contentDescription = contentDescription,
            tint = if (isActive) activeTint else inactiveTint,
            modifier = modifier
                .size(size)
                .graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        )
    }
}
