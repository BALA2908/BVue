package com.bvue.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bvue.ui.anim.bvueGradient
import com.bvue.ui.anim.pressScale
import com.bvue.ui.theme.BVueDisplay

/** Filter chips under the top bar. Selected chip fades from the surface fill to the BVue gradient. */
@Composable
fun ChipRow(
    chips: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = remember { bvueGradient() }
    val base = MaterialTheme.colorScheme.surfaceVariant
    val unselectedText = MaterialTheme.colorScheme.onSurface
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(chips) { chip ->
            val isSelected = chip == selected
            val sel by animateFloatAsState(if (isSelected) 1f else 0f, tween(220), label = "chipSel")
            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .drawBehind {
                        drawRect(base)
                        drawRect(brush = gradient, alpha = sel)
                    }
                    .pressScale { onSelect(chip) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = chip,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = BVueDisplay,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = lerp(unselectedText, Color.White, sel),
                )
            }
        }
    }
}
