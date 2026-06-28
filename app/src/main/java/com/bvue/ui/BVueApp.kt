package com.bvue.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bvue.BVueApplication
import com.bvue.R
import com.bvue.domain.model.AppSettings
import com.bvue.domain.model.ThemeMode
import com.bvue.ui.anim.bvueEnter
import com.bvue.ui.anim.bvueExit
import com.bvue.ui.anim.bvuePopEnter
import com.bvue.ui.anim.bvuePopExit
import com.bvue.ui.channel.ChannelScreen
import com.bvue.ui.components.BVueBottomBar
import com.bvue.ui.intro.BVueIntro
import com.bvue.ui.components.BVueTopBar
import com.bvue.ui.home.HomeScreen
import com.bvue.ui.library.LibraryScreen
import com.bvue.ui.library.PlaylistScreen
import com.bvue.ui.navigation.TopLevelDestination
import com.bvue.ui.player.WatchScreen
import com.bvue.ui.search.SearchScreen
import com.bvue.ui.settings.SettingsScreen
import com.bvue.ui.shorts.ShortsScreen
import com.bvue.ui.subscriptions.SubscriptionsScreen
import com.bvue.ui.theme.BVueTheme
import com.bvue.util.decodeArg
import com.bvue.util.encodeArg

/** Themed root — applies the user's theme preference reactively, then renders the app. */
@Composable
fun BVueRoot() {
    val app = LocalContext.current.applicationContext as BVueApplication
    val settings by app.container.settingsRepository.settings
        .collectAsStateWithLifecycle(initialValue = AppSettings())
    val dark = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    BVueTheme(darkTheme = dark) {
        // Play the branded intro once per cold start. Process-scoped flag (not rememberSaveable) so it
        // survives config changes but replays after a real process restart. Home composes underneath.
        var showIntro by remember {
            val show = !app.container.introShown
            app.container.introShown = true
            mutableStateOf(show)
        }
        Box(Modifier.fillMaxSize()) {
            BVueApp()
            if (showIntro) {
                BVueIntro(onFinished = { showIntro = false })
            }
        }
    }
}

@Composable
fun BVueApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevel = TopLevelDestination.entries.firstOrNull { it.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (currentRoute == TopLevelDestination.HOME.route) {
                BVueTopBar(
                    onSearchClick = { navController.navigate("search") },
                    onAvatarClick = { navController.navigate(TopLevelDestination.YOU.route) },
                )
            }
        },
        bottomBar = {
            if (topLevel != null) {
                BVueBottomBar(
                    current = topLevel,
                    onSelect = { dest ->
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.HOME.route,
            modifier = Modifier.padding(padding),
            enterTransition = { bvueEnter() },
            exitTransition = { bvueExit() },
            popEnterTransition = { bvuePopEnter() },
            popExitTransition = { bvuePopExit() },
        ) {
            composable(TopLevelDestination.HOME.route) {
                HomeScreen(onVideoClick = { id -> navController.navigate("watch/$id") })
            }
            composable(TopLevelDestination.SHORTS.route) { ShortsScreen() }
            composable(TopLevelDestination.CREATE.route) { CenteredLabel(stringResource(R.string.nav_create)) }
            composable(TopLevelDestination.SUBSCRIPTIONS.route) {
                SubscriptionsScreen(onVideoClick = { id -> navController.navigate("watch/$id") })
            }
            composable(TopLevelDestination.YOU.route) {
                LibraryScreen(
                    onVideoClick = { id -> navController.navigate("watch/$id") },
                    onOpenSettings = { navController.navigate("settings") },
                    onPlaylistClick = { id -> navController.navigate("playlist/$id") },
                )
            }
            composable("playlist/{id}") { entry ->
                PlaylistScreen(
                    playlistId = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { id -> navController.navigate("watch/$id") },
                )
            }
            composable("settings") {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable("search") {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { id -> navController.navigate("watch/$id") },
                )
            }
            composable("watch/{videoId}") { entry ->
                WatchScreen(
                    videoId = entry.arguments?.getString("videoId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onChannelClick = { url -> navController.navigate("channel/${encodeArg(url)}") },
                    onVideoClick = { id -> navController.navigate("watch/$id") },
                )
            }
            composable("channel/{ch}") { entry ->
                ChannelScreen(
                    channelUrl = decodeArg(entry.arguments?.getString("ch").orEmpty()),
                    onBack = { navController.popBackStack() },
                    onVideoClick = { id -> navController.navigate("watch/$id") },
                )
            }
        }
    }
}

@Composable
private fun CenteredLabel(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
