package com.bvue.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bvue.R

/**
 * Two families:
 *  - [BVueDisplay] = Outfit (bundled OFL variable font) — the "designed" display face for the wordmark,
 *    titles/headers, and buttons.
 *  - Roboto (system default) — body, metadata, and numeric/duration text (legibility + digit alignment).
 * On API < 26 variable-weight settings are ignored and Outfit renders at its default instance (graceful).
 */
@OptIn(ExperimentalTextApi::class)
val BVueDisplay = FontFamily(
    Font(
        R.font.outfit_variable, weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.outfit_variable, weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.outfit_variable, weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

val BVueTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = BVueDisplay, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle( // watch-screen video title
        fontFamily = BVueDisplay, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle( // channel name, section headers
        fontFamily = BVueDisplay, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle( // search field text, descriptions
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle( // feed video-card title (2 lines) — Roboto for dense readability
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    bodySmall = TextStyle( // metadata line "Channel • N views • time"
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle( // button text (Subscribe, action pills) — display face
        fontFamily = BVueDisplay, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle( // duration badge, player time — Roboto (digits)
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle( // bottom-nav labels
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp,
    ),
)
