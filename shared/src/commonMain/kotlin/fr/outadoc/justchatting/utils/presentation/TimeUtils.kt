package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Stable
@Composable
internal expect fun Instant.formatHourMinute(): String?

@Stable
internal expect fun LocalDate.formatWithoutYear(): String

@Stable
internal expect fun LocalDate.formatWithYear(): String
