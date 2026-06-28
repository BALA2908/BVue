package com.bvue.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// --- Brand constants (theme-independent) ---
val BVueRed = Color(0xFFFF0000)
val BVueRedPressed = Color(0xFFCC0000)

// BVue brand gradient: Canva purple → Instagram pink → Apple-Music/Netflix red → Instagram orange.
val BVueGradientColors = listOf(
    Color(0xFF7D2AE8),
    Color(0xFFE1306C),
    Color(0xFFFA2D48),
    Color(0xFFF77737),
)

// Scrubber colors (used by the player in Phase 2)
val ScrubberTrackLight = Color(0x26000000)
val ScrubberBufferedLight = Color(0x42000000)
val ScrubberTrackDark = Color(0x33FFFFFF)
val ScrubberBufferedDark = Color(0x66FFFFFF)

// --- Light scheme: matches the YouTube mobile light palette ---
val BVueLightColors: ColorScheme = lightColorScheme(
    primary = BVueRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0E0),
    onPrimaryContainer = Color(0xFF410000),
    secondary = Color(0xFF606060),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0F0F0F), // selected chip = dark pill
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF065FD4), // YouTube link blue
    onTertiary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F0F0F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F0F0F),
    surfaceVariant = Color(0xFFF2F2F2), // unselected chip / search field fill
    onSurfaceVariant = Color(0xFF606060), // metadata / inactive nav
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F8F8),
    surfaceContainer = Color(0xFFF2F2F2),
    surfaceContainerHigh = Color(0xFFECECEC),
    surfaceContainerHighest = Color(0xFFE5E5E5),
    surfaceDim = Color(0xFFF0F0F0),
    surfaceBright = Color(0xFFFFFFFF),
    outline = Color(0xFFC6C6C6),
    outlineVariant = Color(0xFFE5E5E5), // hairline dividers
    inverseSurface = Color(0xFF282828),
    inverseOnSurface = Color(0xFFF1F1F1),
    error = Color(0xFFB3261E),
    onError = Color.White,
    scrim = Color(0xFF000000),
)

// --- Dark scheme: matches the YouTube mobile dark palette (#0F0F0F base) ---
val BVueDarkColors: ColorScheme = darkColorScheme(
    primary = BVueRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5A0000),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFAAAAAA),
    onSecondary = Color(0xFF0F0F0F),
    secondaryContainer = Color(0xFFF1F1F1), // selected chip = white pill
    onSecondaryContainer = Color(0xFF0F0F0F),
    tertiary = Color(0xFF3EA6FF),
    onTertiary = Color(0xFF0F0F0F),
    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFF1F1F1),
    surface = Color(0xFF0F0F0F),
    onSurface = Color(0xFFF1F1F1),
    surfaceVariant = Color(0xFF272727),
    onSurfaceVariant = Color(0xFFAAAAAA),
    surfaceContainerLowest = Color(0xFF0F0F0F),
    surfaceContainerLow = Color(0xFF1A1A1A),
    surfaceContainer = Color(0xFF212121),
    surfaceContainerHigh = Color(0xFF282828),
    surfaceContainerHighest = Color(0xFF3D3D3D),
    surfaceDim = Color(0xFF0F0F0F),
    surfaceBright = Color(0xFF383838),
    outline = Color(0xFF3D3D3D),
    outlineVariant = Color(0xFF272727),
    inverseSurface = Color(0xFFF1F1F1),
    inverseOnSurface = Color(0xFF0F0F0F),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    scrim = Color(0xFF000000),
)
