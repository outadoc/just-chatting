package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Composable
@Stable
internal actual fun Instant.formatHourMinute(timeZone: TimeZone): String? {
    val formatter =
        DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withZone(timeZone.toJavaZoneId())

    return remember(this, timeZone) {
        try {
            formatter.format(toJavaInstant())
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
@Stable
internal actual fun Instant.formatFullDateTime(timeZone: TimeZone): String? {
    val formatter =
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
            .withZone(timeZone.toJavaZoneId())

    return remember(this, timeZone) {
        try {
            formatter.format(toJavaInstant())
        } catch (e: Exception) {
            null
        }
    }
}

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
