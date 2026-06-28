package com.bvue.util

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/** Formats an upload time as a YouTube-style relative string, e.g. "3 days ago". */
fun formatRelativeDate(uploaded: OffsetDateTime?): String? {
    if (uploaded == null) return null
    val now = OffsetDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(uploaded, now)
    if (minutes < 0) return null
    val days = ChronoUnit.DAYS.between(uploaded, now)
    return when {
        days >= 365 -> plural(days / 365, "year")
        days >= 30 -> plural(days / 30, "month")
        days >= 7 -> plural(days / 7, "week")
        days >= 1 -> plural(days, "day")
        else -> {
            val hours = ChronoUnit.HOURS.between(uploaded, now)
            when {
                hours >= 1 -> plural(hours, "hour")
                minutes >= 1 -> plural(minutes, "minute")
                else -> "just now"
            }
        }
    }
}

private fun plural(n: Long, unit: String): String = "$n $unit${if (n == 1L) "" else "s"} ago"
