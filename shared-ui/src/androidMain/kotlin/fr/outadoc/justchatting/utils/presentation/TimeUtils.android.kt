package fr.outadoc.justchatting.utils.presentation

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Stable
@Composable
internal actual fun Instant.formatHourMinute(): String? {
    val context = LocalContext.current
    val format = remember { DateFormat.getTimeFormat(context) }
    return remember(this) {
        try {
            format.format(Date.from(toJavaInstant()))
        } catch (e: Exception) {
            null
        }
    }
}

@Stable
@Composable
internal actual fun Instant.formatFullDateTime(): String? {
    val context = LocalContext.current
    val dateFormat = remember { DateFormat.getLongDateFormat(context) }
    val timeFormat = remember { DateFormat.getTimeFormat(context) }
    return remember(this) {
        try {
            val date = Date.from(toJavaInstant())
            "${dateFormat.format(date)} ${timeFormat.format(date)}"
        } catch (e: Exception) {
            null
        }
    }
}

@Stable
internal actual fun LocalDate.formatWithoutYear(): String =
    toJavaLocalDate()
        .format(
            DateTimeFormatter.ofPattern("eeee d MMM", Locale.getDefault()),
        )

@Stable
internal actual fun LocalDate.formatWithYear(): String =
    toJavaLocalDate()
        .format(
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.getDefault()),
        )
