package com.github.andreyasadchy.xtra.util

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Date

fun Instant.formatTimestamp(context: Context): String? {
    val format = DateFormat.getTimeFormat(context)
    return try {
        format.format(Date.from(toJavaInstant()))
    } catch (e: Exception) {
        null
    }
}

fun Instant.formatTime(
    context: Context,
    tz: TimeZone = TimeZone.currentSystemDefault(),
    clock: Clock = Clock.System
): String {
    val isCurrentYear = toLocalDateTime(tz).year == clock.now().toLocalDateTime(tz).year

    return DateUtils.formatDateTime(
        context,
        toEpochMilliseconds(),
        if (isCurrentYear) {
            DateUtils.FORMAT_NO_YEAR
        } else {
            DateUtils.FORMAT_SHOW_DATE
        }
    )
}
