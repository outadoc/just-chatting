package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.icerock.moko.resources.format
import fr.outadoc.justchatting.shared.MR
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

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
