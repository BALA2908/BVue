package com.bvue.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bvue.ui.anim.Motion
import com.bvue.ui.anim.bvueGradient
import com.bvue.ui.navigation.TopLevelDestination

/**
 * Bottom navigation with a BVue-gradient indicator pill behind the selected icon, a spring scale-pop,
 * and a crossfade between the outlined/filled icons.
 */
@Composable
fun BVueBottomBar(
    current: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = remember { bvueGradient() }
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
    ) {
        val inactive = MaterialTheme.colorScheme.onSurfaceVariant
        TopLevelDestination.entries.forEach { dest ->
            val selected = dest == current
            val sel by animateFloatAsState(if (selected) 1f else 0f, tween(240), label = "navSel")
            val pop by animateFloatAsState(if (selected) 1.1f else 1f, Motion.springBouncy(), label = "navPop")
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(dest) },
                icon = {
                    Box(
                        modifier = Modifier.graphicsLayer { scaleX = pop; scaleY = pop },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier
                                .size(width = 52.dp, height = 30.dp)
                                .clip(RoundedCornerShape(50))
                                .drawBehind { drawRect(brush = gradient, alpha = sel) },
                        )
                        Crossfade(targetState = selected, label = "navIcon") { s ->
                            Icon(
                                imageVector = if (s) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = stringResource(dest.labelRes),
                                tint = lerp(inactive, Color.White, sel),
                            )
                        }
                    }
                },
                label = {
                    Text(stringResource(dest.labelRes), style = MaterialTheme.typography.labelSmall)
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}
