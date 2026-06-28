package com.bvue.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.ui.graphics.vector.ImageVector
import com.bvue.R

/** The five bottom-nav destinations, laid out exactly like the YouTube app. */
enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("home", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    // Shorts gets a custom glyph in Phase 6; PlayCircle stands in for now.
    SHORTS("shorts", R.string.nav_shorts, Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle),
    CREATE("create", R.string.nav_create, Icons.Outlined.AddCircle, Icons.Outlined.AddCircle),
    SUBSCRIPTIONS("subscriptions", R.string.nav_subscriptions, Icons.Filled.Subscriptions, Icons.Outlined.Subscriptions),
    YOU("you", R.string.nav_you, Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle),
}
