package com.bvue.ui.anim

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

private const val NAV_D = 280

private val topLevelRoutes = setOf("home", "shorts", "create", "subscriptions", "you")

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
    initialState.destination.route in topLevelRoutes && targetState.destination.route in topLevelRoutes

/** Forward enter: fade-through between tabs, slide-up "hero" for a video, shared-axis otherwise. */
fun AnimatedContentTransitionScope<NavBackStackEntry>.bvueEnter(): EnterTransition {
    val target = targetState.destination.route
    return when {
        isTabSwitch() -> fadeIn(tween(NAV_D, easing = Motion.Emphasized))
        target?.startsWith("watch/") == true ->
            fadeIn(tween(NAV_D)) +
                slideIntoContainer(SlideDirection.Up, tween(NAV_D, easing = Motion.Emphasized)) { (it * 0.2f).toInt() }
        else ->
            fadeIn(tween(NAV_D, easing = Motion.Emphasized)) +
                slideIntoContainer(SlideDirection.Start, tween(NAV_D, easing = Motion.Emphasized)) { (it * 0.14f).toInt() }
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.bvueExit(): ExitTransition =
    if (isTabSwitch()) {
        fadeOut(tween(NAV_D))
    } else {
        fadeOut(tween(NAV_D, easing = Motion.Emphasized)) +
            slideOutOfContainer(SlideDirection.Start, tween(NAV_D)) { (it * 0.10f).toInt() }
    }

fun AnimatedContentTransitionScope<NavBackStackEntry>.bvuePopEnter(): EnterTransition =
    fadeIn(tween(NAV_D, easing = Motion.Emphasized)) +
        slideIntoContainer(SlideDirection.End, tween(NAV_D)) { (it * 0.10f).toInt() }

fun AnimatedContentTransitionScope<NavBackStackEntry>.bvuePopExit(): ExitTransition {
    val leaving = initialState.destination.route
    return if (leaving?.startsWith("watch/") == true) {
        fadeOut(tween(NAV_D)) +
            slideOutOfContainer(SlideDirection.Down, tween(NAV_D, easing = Motion.Emphasized)) { (it * 0.2f).toInt() }
    } else {
        fadeOut(tween(NAV_D, easing = Motion.Emphasized)) +
            slideOutOfContainer(SlideDirection.End, tween(NAV_D)) { (it * 0.14f).toInt() }
    }
}
