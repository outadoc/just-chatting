package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Composable
@Stable
internal actual fun Instant.formatHourMinute(): String? {
    val formatter =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())

    return remember(this) {
        try {
            formatter.format(toJavaInstant())
        } catch (e: Exception) {
            null
        }
    }
}

@Stable
internal actual fun LocalDate.formatWithoutYear(): String {
    return toJavaLocalDate()
        .format(
            DateTimeFormatter.ofPattern("eeee d MMM", Locale.getDefault()),
        )
}

@Stable
internal actual fun LocalDate.formatWithYear(): String {
    return toJavaLocalDate()
        .format(
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.getDefault()),
        )
}
