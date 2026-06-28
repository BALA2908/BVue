package com.bvue.ui.intro

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.bvue.ui.anim.Motion
import com.bvue.ui.theme.BVueDisplay
import com.bvue.ui.theme.BVueGradientColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Deep brand background the system splash also uses, so the handoff has no color flash. */
val IntroBackground = Color(0xFF120324)

/**
 * Cold-start brand intro: the play triangle draws itself stroke-by-stroke onto the gradient badge,
 * the badge pops, the "BVue" wordmark reveals, then the whole thing expands + fades into the app.
 * ~1.1s; tap anywhere to skip. Gated to once-per-process by the caller.
 */
@Composable
fun BVueIntro(onFinished: () -> Unit) {
    val drawProgress = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(0.7f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }
    var skip by remember { mutableStateOf(false) }

    // Status-bar / nav icons must be light over the dark intro; restore previous on dispose.
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val prevStatus = controller.isAppearanceLightStatusBars
        val prevNav = controller.isAppearanceLightNavigationBars
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        onDispose {
            controller.isAppearanceLightStatusBars = prevStatus
            controller.isAppearanceLightNavigationBars = prevNav
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { badgeScale.animateTo(1f, Motion.springBouncy()) }
            drawProgress.animateTo(1f, tween(480, easing = Motion.Emphasized))
        }
        if (!skip) {
            badgeScale.animateTo(1.12f, tween(110))
            badgeScale.animateTo(1f, Motion.springBouncy())
        }
        if (!skip) wordmarkAlpha.animateTo(1f, tween(240))
        if (!skip) delay(240)
        exit.animateTo(1f, tween(if (skip) 160 else 340, easing = Motion.Emphasized))
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 1f - exit.value
                val s = 1f + 0.18f * exit.value
                scaleX = s
                scaleY = s
            }
            .background(IntroBackground)
            .pointerInput(Unit) { detectTapGestures { skip = true } },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(
                Modifier
                    .size(104.dp)
                    .graphicsLayer { scaleX = badgeScale.value; scaleY = badgeScale.value },
            ) {
                val w = size.width
                val h = size.height
                drawRoundRect(
                    brush = Brush.linearGradient(BVueGradientColors, Offset(0f, 0f), Offset(w, h)),
                    cornerRadius = CornerRadius(w * 0.26f, w * 0.26f),
                )
                val triangle = Path().apply {
                    moveTo(w * 0.40f, h * 0.30f)
                    lineTo(w * 0.40f, h * 0.70f)
                    lineTo(w * 0.67f, h * 0.50f)
                    close()
                }
                // Fill fades in over the last 20% of the draw, so it "draws then fills".
                val fillAlpha = ((drawProgress.value - 0.8f) / 0.2f).coerceIn(0f, 1f)
                if (fillAlpha > 0f) drawPath(triangle, Color.White.copy(alpha = fillAlpha))
                val measure = PathMeasure().apply { setPath(triangle, false) }
                val segment = Path()
                measure.getSegment(0f, measure.length * drawProgress.value, segment, true)
                drawPath(
                    segment,
                    Color.White,
                    style = Stroke(width = w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = "BVue",
                modifier = Modifier.graphicsLayer { alpha = wordmarkAlpha.value },
                style = TextStyle(
                    brush = Brush.linearGradient(BVueGradientColors),
                    fontFamily = BVueDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                    letterSpacing = (-1).sp,
                ),
            )
        }
    }
}
