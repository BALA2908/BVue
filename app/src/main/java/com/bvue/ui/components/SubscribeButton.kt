package com.bvue.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bvue.ui.anim.bvueGradient
import com.bvue.ui.anim.pressScale

/**
 * Shared Subscribe toggle. Unsubscribed = a BVue-gradient CTA (the highest-signal gradient placement);
 * subscribed = a neutral tonal button. Used on the watch screen and channel pages.
 */
@Composable
fun SubscribeButton(subscribed: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (subscribed) {
        FilledTonalButton(onClick = onClick, modifier = modifier) { Text("Subscribed") }
    } else {
        val gradient = bvueGradient()
        Box(
            modifier
                .clip(RoundedCornerShape(50))
                .drawBehind { drawRect(gradient) }
                .pressScale(onClick = onClick)
                .padding(horizontal = 22.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Subscribe", color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}
