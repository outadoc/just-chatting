package com.github.andreyasadchy.xtra.util

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.github.andreyasadchy.xtra.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun getDurationFromSeconds(context: Context, input: String?, text: Boolean = true): String? {
    if (input != null) {
        val duration = try {
            Integer.parseInt(input)
        } catch (e: NumberFormatException) {
            return null
        }
        val days = (duration / 86400)
        val hours = ((duration % 86400) / 3600)
        val minutes = (((duration % 86400) % 3600) / 60)
        val seconds = (duration % 60)
        return if (text) String.format(
            (if (days != 0) (days.toString() + context.getString(R.string.days) + " ") else "") +
                    (if (hours != 0) (hours.toString() + context.getString(R.string.hours) + " ") else "") +
                    (if (minutes != 0) (minutes.toString() + context.getString(R.string.minutes) + " ") else "") +
                    (if (seconds != 0) (seconds.toString() + context.getString(R.string.seconds) + " ") else "")
        ).trim() else
            String.format(
                (if (days != 0) ("$days:") else "") +
                        (if (hours != 0) (if (hours < 10 && days != 0) "0$hours:" else "$hours:") else (if (days != 0) "00:" else "")) +
                        (if (minutes != 0) (if (minutes < 10 && (hours != 0 || days != 0)) "0$minutes:" else "$minutes:") else (if (hours != 0 || days != 0) "00:" else "")) +
                        (if (seconds != 0) (if (seconds < 10 && (minutes != 0 || hours != 0 || days != 0)) "0$seconds" else "$seconds") else (if (minutes != 0 || hours != 0 || days != 0) "00" else ""))
            )
    } else return null
}

fun getUptime(context: Context, input: String?): String? {
    return if (input != null) {
        val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time.time
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        val createdAt = try {
            format.parse(input)?.time
        } catch (e: ParseException) {
            null
        }
        val diff = if (createdAt != null) ((currentTime - createdAt) / 1000) else null
        return if (diff != null && diff >= 0) {
            getDurationFromSeconds(context, diff.toString(), false)
        } else null
    } else null
}

fun getTimestamp(context: Context, input: Long): String? {
    val format = DateFormat.getTimeFormat(context)
    return try {
        format.format(Date(input))
    } catch (e: Exception) {
        null
    }
}

fun parseIso8601Date(date: String): Long? {
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(date)?.time
    } catch (e: ParseException) {
        try {
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                Locale.getDefault()
            ).parse(date)?.time
        } catch (e: ParseException) {
            null
        }
    }
}

fun formatTimeString(context: Context, iso8601date: String): String? {
    return parseIso8601Date(iso8601date)?.let { formatTime(context, it) }
}

private fun formatTime(context: Context, date: Long): String {
    val year = Calendar.getInstance().let {
        it.timeInMillis = date
        it.get(Calendar.YEAR)
    }
    val format = if (year == Calendar.getInstance().get(Calendar.YEAR)) {
        DateUtils.FORMAT_NO_YEAR
    } else {
        DateUtils.FORMAT_SHOW_DATE
    }
    return DateUtils.formatDateTime(context, date, format)
}
