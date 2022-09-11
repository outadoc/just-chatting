package fr.outadoc.justchatting.util

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun Instant.formatTimestamp(): String? {
    val format = DateFormat.getTimeFormat(LocalContext.current)
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
