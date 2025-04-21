package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Composable
@Stable
internal actual fun Instant.formatHourMinute(): String? {
    // TODO
    return toString()
}

@Stable
internal actual fun LocalDate.formatWithoutYear(): String {
    // TODO
    return toString()
}

@Stable
internal actual fun LocalDate.formatWithYear(): String {
    // TODO
    return toString()
}
