package com.bvue.ui.anim

import androidx.compose.ui.graphics.Brush
import com.bvue.ui.theme.BVueGradientColors

/** The BVue brand gradient as a linear brush spanning the drawn area. */
fun bvueGradient(): Brush = Brush.linearGradient(BVueGradientColors)

/** The brand gradient as a closed sweep — for the pull-to-refresh ring. */
fun bvueSweep(): Brush = Brush.sweepGradient(BVueGradientColors + BVueGradientColors.first())
