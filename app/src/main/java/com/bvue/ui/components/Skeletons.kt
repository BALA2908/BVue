package com.bvue.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.bvue.ui.anim.shimmer

/** A single shimmering placeholder block. Uses onSurface@8% so it reads on both light and dark. */
@Composable
private fun Bone(modifier: Modifier, shape: Shape = RoundedCornerShape(4.dp)) {
    androidx.compose.foundation.layout.Box(
        modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .shimmer(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
    )
}

/** Mirrors VideoCard's layout exactly so the loading→content swap doesn't reflow. */
@Composable
fun VideoCardSkeleton() {
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Bone(Modifier.fillMaxWidth().aspectRatio(16f / 9f), RectangleShape)
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 12.dp)) {
            Bone(Modifier.size(36.dp), CircleShape)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Bone(Modifier.fillMaxWidth(0.9f).height(14.dp))
                Spacer(Modifier.height(8.dp))
                Bone(Modifier.fillMaxWidth(0.5f).height(12.dp))
            }
        }
    }
}

/** A feed of skeleton cards. Plain Column (not Lazy) — only the loading branch composes it, so the
 *  shimmer's infinite transition stops the moment real content arrives. */
@Composable
fun FeedSkeleton(count: Int = 6, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        repeat(count) { VideoCardSkeleton() }
    }
}
