package fr.outadoc.justchatting.utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import kotlin.time.Instant

@Stable
@Composable
internal actual fun Instant.formatHourMinute(): String? {
    val formatter = remember {
        NSDateFormatter().apply {
            dateFormat = "HH:mm"
            locale = NSLocale.currentLocale
        }
    }

    return remember(this) {
        formatter.stringFromDate(this.toNSDate())
    }
}

@Stable
internal actual fun LocalDate.formatWithoutYear(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "eeee d MMM"
        locale = NSLocale.currentLocale
    }

    val instant: Instant = this.atStartOfDayIn(TimeZone.currentSystemDefault())
    val date = instant.toNSDate()

    return formatter.stringFromDate(date)
}

@Stable
internal actual fun LocalDate.formatWithYear(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "d MMM uuuu"
        locale = NSLocale.currentLocale
    }

    val instant: Instant = this.atStartOfDayIn(TimeZone.currentSystemDefault())
    val date = instant.toNSDate()

    return formatter.stringFromDate(date)
}
