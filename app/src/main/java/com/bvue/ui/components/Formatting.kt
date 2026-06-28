package com.bvue.ui.components

import java.util.Locale

/** "3:42" or "1:02:05". Returns "" for live/unknown (duration <= 0). */
fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return ""
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.US, "%d:%02d", m, s)
    }
}

/** "1.2M", "15K", "342". */
fun formatCount(count: Long): String {
    if (count < 0) return ""
    return when {
        count >= 1_000_000_000 -> trimZero(String.format(Locale.US, "%.1fB", count / 1_000_000_000.0))
        count >= 1_000_000 -> trimZero(String.format(Locale.US, "%.1fM", count / 1_000_000.0))
        count >= 1_000 -> trimZero(String.format(Locale.US, "%.1fK", count / 1_000.0))
        else -> count.toString()
    }
}

private fun trimZero(s: String): String =
    s.replace(".0B", "B").replace(".0M", "M").replace(".0K", "K")
