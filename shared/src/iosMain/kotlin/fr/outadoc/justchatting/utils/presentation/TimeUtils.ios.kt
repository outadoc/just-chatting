package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@Composable
internal actual fun Instant.formatHourMinute(): String? {
    TODO("not implemented")
}

@Composable
internal actual fun Instant.formatDate(
    tz: TimeZone,
    clock: Clock,
): String {
    TODO("not implemented")
}

@Composable
internal actual fun LocalDate.formatDate(
    tz: TimeZone,
    clock: Clock,
    isFuture: Boolean,
): String {
    TODO("not implemented")
}
