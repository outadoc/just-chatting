package fr.outadoc.justchatting.utils.ui

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.duration_days
import fr.outadoc.justchatting.shared.duration_hours
import fr.outadoc.justchatting.shared.duration_minutes
import fr.outadoc.justchatting.shared.duration_seconds
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
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
fun Instant.formatDate(
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

@Composable
fun Duration.format(): String =
    buildList {
        toComponents { days, hours, minutes, seconds, _ ->
            days.takeIf { it > 0 }?.let { d ->
                add(stringResource(Res.string.duration_days, d))
            }

            hours.takeIf { it > 0 }?.let { h ->
                add(stringResource(Res.string.duration_hours, h))
            }

            minutes.takeIf { it > 0 }?.let { m ->
                add(stringResource(Res.string.duration_minutes, m))
            }

            seconds.takeIf { it > 0 }?.let { s ->
                add(stringResource(Res.string.duration_seconds, s))
            }
        }
    }.joinToString(" ")
