package fr.outadoc.justchatting.utils.presentation

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.shared.MR
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.minutes

@Composable
internal actual fun Instant.formatHourMinute(): String? {
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
internal actual fun Instant.formatDate(
    tz: TimeZone,
    clock: Clock,
): String {
    var now by remember { mutableStateOf(clock.now()) }
    val today = remember(now) { now.toLocalDateTime(tz).date }

    LaunchedEffect(clock) {
        while (isActive) {
            now = clock.now()
            delay(1.minutes)
        }
    }

    return toLocalDateTime(tz).date
        .formatDate(
            today = today,
            isFuture = this > now,
        )
}

@Composable
internal actual fun LocalDate.formatDate(
    tz: TimeZone,
    clock: Clock,
    isFuture: Boolean,
): String {
    var today by remember { mutableStateOf(clock.now().toLocalDateTime(tz).date) }

    LaunchedEffect(clock) {
        while (isActive) {
            today = clock.now().toLocalDateTime(tz).date
            delay(1.minutes)
        }
    }

    return formatDate(
        today = today,
        isFuture = isFuture,
    )
}

/**
 * Format a date in a human-readable way.
 *
 * If the date is today, return "Today".
 * If the date is yesterday, return "Yesterday".
 * Otherwise, if the date is within the current year, return the date without the year.
 * Otherwise, return the full date.
 */
@Composable
private fun LocalDate.formatDate(
    today: LocalDate,
    isFuture: Boolean,
): String {
    val isToday = this == today
    val isYesterday = this == (today - DatePeriod(days = 1))
    val isTomorrow = this == (today + DatePeriod(days = 1))
    val isCurrentYear = year == today.year

    return when {
        isYesterday -> {
            stringResource(MR.strings.date_yesterday)
        }

        isToday && !isFuture -> {
            stringResource(MR.strings.date_today_earlier)
        }

        isToday && isFuture -> {
            stringResource(MR.strings.date_today_later)
        }

        isTomorrow -> {
            stringResource(MR.strings.date_tomorrow)
        }

        isCurrentYear -> {
            toJavaLocalDate().format(
                DateTimeFormatter.ofPattern("eeee d MMM", Locale.getDefault()),
            )
        }

        else -> {
            toJavaLocalDate().format(
                DateTimeFormatter.ofPattern("d MMM uuuu", Locale.getDefault()),
            )
        }
    }
}
