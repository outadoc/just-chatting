package fr.outadoc.justchatting.utils.presentation

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import java.util.TimeZone as JavaTimeZone

@Stable
@Composable
internal actual fun Instant.formatHourMinute(timeZone: TimeZone): String? {
    val context = LocalContext.current
    val format =
        remember(timeZone) {
            DateFormat.getTimeFormat(context).apply {
                this.timeZone = timeZone.toJavaTimeZone()
            }
        }
    return remember(this, format) {
        try {
            format.format(Date.from(toJavaInstant()))
        } catch (e: Exception) {
            null
        }
    }
}

@Stable
@Composable
internal actual fun Instant.formatFullDateTime(timeZone: TimeZone): String? {
    val context = LocalContext.current
    val dateFormat =
        remember(timeZone) {
            DateFormat.getLongDateFormat(context).apply {
                this.timeZone = timeZone.toJavaTimeZone()
            }
        }
    val timeFormat =
        remember(timeZone) {
            DateFormat.getTimeFormat(context).apply {
                this.timeZone = timeZone.toJavaTimeZone()
            }
        }
    return remember(this, dateFormat, timeFormat) {
        try {
            val date = Date.from(toJavaInstant())
            "${dateFormat.format(date)} ${timeFormat.format(date)}"
        } catch (e: Exception) {
            null
        }
    }
}

private fun TimeZone.toJavaTimeZone(): JavaTimeZone = JavaTimeZone.getTimeZone(toJavaZoneId())

@Stable
internal actual fun LocalDate.formatWithoutYear(): String =
    toJavaLocalDate()
        .format(
            DateTimeFormatter.ofPattern("eeee d MMM", Locale.getDefault()),
        )

@Stable
internal actual fun LocalDate.formatWithYear(): String =
    toJavaLocalDate()
        .format(
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.getDefault()),
        )

@Stable
internal actual fun LocalDate.formatShortDay(): String =
    toJavaLocalDate()
        .format(
            DateTimeFormatter.ofPattern("eee d MMM", Locale.getDefault()),
        )
