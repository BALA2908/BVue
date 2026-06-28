package com.bvue.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bvue.R
import com.bvue.ui.theme.BVueGradientColors

/** YouTube-style top app bar: logo left; cast / notifications / search / avatar right. */
@Composable
fun BVueTopBar(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onCastClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BVueLogo()
            Spacer(Modifier.weight(1f))
            val tint = MaterialTheme.colorScheme.onSurface
            IconButton(onClick = onCastClick) {
                Icon(Icons.Outlined.Cast, stringResource(R.string.cd_cast), tint = tint)
            }
            IconButton(onClick = onNotificationsClick) {
                Icon(Icons.Outlined.Notifications, stringResource(R.string.cd_notifications), tint = tint)
            }
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Outlined.Search, stringResource(R.string.cd_search), tint = tint)
            }
            Box(
                modifier = Modifier
                    .padding(start = 4.dp, end = 8.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(brush = Brush.linearGradient(BVueGradientColors)),
                contentAlignment = Alignment.Center,
            ) {
                Text("B", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
