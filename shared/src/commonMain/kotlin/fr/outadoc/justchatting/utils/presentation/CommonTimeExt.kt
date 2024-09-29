package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.format
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
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Composable
internal fun LocalDate.formatDate(
    tz: TimeZone = TimeZone.currentSystemDefault(),
    clock: Clock = Clock.System,
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

@Composable
internal fun Instant.formatDate(
    tz: TimeZone = TimeZone.currentSystemDefault(),
    clock: Clock = Clock.System,
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

@Stable
@Composable
internal fun Duration.format(
    showSeconds: Boolean = true,
): String {
    return toComponents { days, hours, minutes, seconds, _ ->
        listOfNotNull(
            days
                .takeIf { it > 0 }
                ?.let { MR.strings.duration_days.format(it).asString() },
            hours
                .takeIf { it > 0 }
                ?.let { MR.strings.duration_hours.format(it).asString() },
            minutes
                .takeIf { it > 0 }
                ?.let { MR.strings.duration_minutes.format(it).asString() },
            seconds
                .takeIf { it > 0 }
                .takeIf { showSeconds }
                ?.let { MR.strings.duration_seconds.format(it).asString() },
        )
    }.joinToString(" ")
}

@Composable
internal fun Instant.formatTimeSince(
    clock: Clock = Clock.System,
    showSeconds: Boolean,
): String {
    var now by remember { mutableStateOf(clock.now()) }
    val duration = remember(now) { now - this }

    LaunchedEffect(clock) {
        while (isActive) {
            now = clock.now()
            delay(1.minutes)
        }
    }

    return duration.format(
        showSeconds = showSeconds,
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
@Stable
@Composable
internal fun LocalDate.formatDate(
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
            formatWithoutYear()
        }

        else -> {
            formatWithYear()
        }
    }
}
