package com.bvue.ui.anim

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Shared motion tokens. Data-only (no composables) so referencing them costs nothing at recomposition.
 * All animated scale/alpha/translation should be applied via Modifier.graphicsLayer (draw phase).
 */
object Motion {
    const val DurationFast = 180
    const val DurationMed = 260
    const val DurationSlow = 320

    /** M3 emphasized-decelerate — for nav transitions and the intro exit. */
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Overshoot spring — badge pop, like/favorite bounce. */
    fun <T> springBouncy(): SpringSpec<T> =
        spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium)

    /** Tight spring — press-scale, selection pop. */
    fun <T> springSnappy(): SpringSpec<T> =
        spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
}
