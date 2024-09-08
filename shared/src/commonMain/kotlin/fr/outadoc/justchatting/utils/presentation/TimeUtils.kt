package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Duration

@Composable
internal expect fun Instant.formatHourMinute(): String?

@Composable
internal expect fun Instant.formatDate(
    tz: TimeZone = TimeZone.currentSystemDefault(),
    clock: Clock = Clock.System,
): String

@Composable
internal expect fun LocalDate.formatDate(
    tz: TimeZone = TimeZone.currentSystemDefault(),
    clock: Clock = Clock.System,
    isFuture: Boolean,
): String

@Composable
internal expect fun Duration.format(showSeconds: Boolean = true): String
