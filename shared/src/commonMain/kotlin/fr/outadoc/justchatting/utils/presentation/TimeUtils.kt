package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Composable
internal expect fun Instant.formatHourMinute(): String?

internal expect fun LocalDate.formatWithoutYear(): String

internal expect fun LocalDate.formatWithYear(): String
