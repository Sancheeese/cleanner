package com.sancheeese.cleanner.ui.screens

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatBytes(bytes: Long): String {
    val gb = 1024.0 * 1024.0 * 1024.0
    val mb = 1024.0 * 1024.0
    val kb = 1024.0
    return when {
        bytes >= gb -> "%.2f GB".format(bytes / gb)
        bytes >= mb -> "%.1f MB".format(bytes / mb)
        bytes >= kb -> "%.1f KB".format(bytes / kb)
        else -> "$bytes B"
    }
}

fun formatInstant(instant: Instant): String {
    return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(instant)
}
