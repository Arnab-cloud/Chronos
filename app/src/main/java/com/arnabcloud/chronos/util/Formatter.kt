package com.arnabcloud.chronos.util

import java.time.Duration

fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0 || hours == 0L) append("${minutes}m")
    }.trim()
}
