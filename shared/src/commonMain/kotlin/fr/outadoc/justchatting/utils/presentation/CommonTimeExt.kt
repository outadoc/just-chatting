package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.date_today_earlier
import fr.outadoc.justchatting.shared.date_today_later
import fr.outadoc.justchatting.shared.date_tomorrow
import fr.outadoc.justchatting.shared.date_yesterday
import fr.outadoc.justchatting.shared.duration_days
import fr.outadoc.justchatting.shared.duration_hours
import fr.outadoc.justchatting.shared.duration_minutes
import fr.outadoc.justchatting.shared.duration_seconds
import fr.outadoc.justchatting.utils.resources.desc
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

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

    return toLocalDateTime(tz)
        .date
        .formatDate(
            today = today,
            isFuture = this > now,
        )
}

@Stable
@Composable
internal fun Duration.format(showSeconds: Boolean = true): String = toComponents { days, hours, minutes, seconds, _ ->
    listOfNotNull(
        days
            .takeIf { it > 0 }
            ?.let { Res.string.duration_days.desc(it) },
        hours
            .takeIf { it > 0 }
            ?.let { Res.string.duration_hours.desc(it) },
        minutes
            .takeIf { it > 0 }
            ?.let { Res.string.duration_minutes.desc(it) },
        seconds
            .takeIf { it > 0 }
            .takeIf { showSeconds }
            ?.let { Res.string.duration_seconds.desc(it) },
    )
}.map { desc -> desc.localized() }
    .joinToString(" ")

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
            stringResource(Res.string.date_yesterday)
        }

        isToday && !isFuture -> {
            stringResource(Res.string.date_today_earlier)
        }

        isToday && isFuture -> {
            stringResource(Res.string.date_today_later)
        }

        isTomorrow -> {
            stringResource(Res.string.date_tomorrow)
        }

        isCurrentYear -> {
            formatWithoutYear()
        }

        else -> {
            formatWithYear()
        }
    }
}
