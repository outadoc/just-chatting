package fr.outadoc.justchatting.utils.presentation

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.format
import fr.outadoc.justchatting.shared.MR
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Date
import kotlin.time.Duration

@Composable
internal fun Instant.formatTimestamp(): String? {
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
internal fun Instant.formatDate(
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

internal fun Duration.format(context: Context): String =
    sequence {
        toComponents { days, hours, minutes, seconds, _ ->
            days.takeIf { it > 0 }?.let {
                yield(MR.strings.duration_days.format(it).toString(context))
            }

            hours.takeIf { it > 0 }?.let {
                yield(MR.strings.duration_hours.format(it).toString(context))
            }

            minutes.takeIf { it > 0 }?.let {
                yield(MR.strings.duration_minutes.format(it).toString(context))
            }

            seconds.takeIf { it > 0 }?.let {
                yield(MR.strings.duration_seconds.format(it).toString(context))
            }
        }
    }.joinToString(" ")
