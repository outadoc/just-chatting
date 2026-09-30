package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/**
 * Formats the time of this instant in [timeZone], e.g. "14:05".
 */
@Stable
@Composable
internal expect fun Instant.formatHourMinute(timeZone: TimeZone): String?

/**
 * Formats the date and time of this instant in [timeZone].
 */
@Stable
@Composable
internal expect fun Instant.formatFullDateTime(timeZone: TimeZone): String?

@Stable
internal expect fun LocalDate.formatWithoutYear(): String

@Stable
internal expect fun LocalDate.formatWithYear(): String

/**
 * Formats a date as a short day label, e.g. "Wed 30 Sep".
 */
@Stable
internal expect fun LocalDate.formatShortDay(): String
