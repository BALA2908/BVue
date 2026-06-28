package com.bvue.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bvue.ui.theme.BVueDisplay
import com.bvue.ui.theme.BVueGradientColors

/**
 * BVue brand lockup: a gradient play-button badge + the "BVue" wordmark rendered with the same
 * gradient (the Netflix/Instagram/Canva/Apple-Music-inspired identity).
 */
@Composable
fun BVueLogo(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(width = 30.dp, height = 21.dp)) {
            val w = size.width
            val h = size.height
            val brush = Brush.linearGradient(BVueGradientColors, start = Offset(0f, 0f), end = Offset(w, h))
            drawRoundRect(brush = brush, cornerRadius = CornerRadius(w * 0.24f, w * 0.24f))
            val triangle = Path().apply {
                moveTo(w * 0.40f, h * 0.28f)
                lineTo(w * 0.40f, h * 0.72f)
                lineTo(w * 0.66f, h * 0.50f)
                close()
            }
            drawPath(triangle, Color.White)
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = "BVue",
            style = TextStyle(
                brush = Brush.linearGradient(BVueGradientColors),
                fontFamily = BVueDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = (-0.8).sp,
            ),
        )
    }
}
