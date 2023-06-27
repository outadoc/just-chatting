package fr.outadoc.justchatting.utils.ui

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Date
import kotlin.time.Duration

@Composable
fun Instant.formatTimestamp(): String? {
    val context = LocalContext.current
    val format = remember { DateFormat.getTimeFormat(context) }
    return remember(this) {
        try {
            format.format(Date.from(toJavaInstant()))
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun Instant.formatTime(
    tz: TimeZone = TimeZone.currentSystemDefault(),
    clock: Clock = Clock.System,
): String? {
    val context = LocalContext.current
    return remember(this, tz, clock) {
        val isCurrentYear = toLocalDateTime(tz).year == clock.now().toLocalDateTime(tz).year
        DateUtils.formatDateTime(
            context,
            toEpochMilliseconds(),
            if (isCurrentYear) {
                DateUtils.FORMAT_NO_YEAR
            } else {
                DateUtils.FORMAT_SHOW_DATE
            },
        )
    }
}

fun Duration.format(context: Context): String =
    sequence {
        toComponents { days, hours, minutes, seconds, _ ->
            days.takeIf { it > 0 }?.let {
                yield(context.resources.getString(R.string.duration_days, it))
            }

            hours.takeIf { it > 0 }?.let {
                yield(context.resources.getString(R.string.duration_hours, it))
            }

            minutes.takeIf { it > 0 }?.let {
                yield(context.resources.getString(R.string.duration_minutes, it))
            }

            seconds.takeIf { it > 0 }?.let {
                yield(context.resources.getString(R.string.duration_seconds, it))
            }
        }
    }.joinToString(" ")