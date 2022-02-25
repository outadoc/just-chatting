package com.github.andreyasadchy.xtra.util

import android.content.Context
import android.text.format.DateUtils
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.util.chat.*
import java.lang.Integer.parseInt
import java.lang.Long.parseLong
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object TwitchApiHelper {

    var checkedValidation = false

    fun getTemplateUrl(url: String?, type: String): String? {
        if ((url == null)||(url == "")||(url.startsWith("https://vod-secure.twitch.tv/_404/404_processing")))
            return when (type) {
                "game" -> "https://static-cdn.jtvnw.net/ttv-static/404_boxart.jpg"
                "video" -> "https://vod-secure.twitch.tv/_404/404_processing_320x180.png"
                else -> null
            }
        val width = when (type) {
            "game" -> "285"
            "video" -> "1280"
            "profileimage" -> "300"
            else -> "" }
        val height = when (type) {
            "game" -> "380"
            "video" -> "720"
            "profileimage" -> "300"
            else -> "" }
        val reg1 = """-\d\d\dx\d\d\d""".toRegex()
        val reg2 = """\d\d\d\dx\d\d\d""".toRegex()
        val reg3 = """\d\d\dx\d\d\d""".toRegex()
        val reg4 = """\d\dx\d\d\d""".toRegex()
        val reg5 = """\d\d\dx\d\d""".toRegex()
        val reg6 = """\d\dx\d\d""".toRegex()
        if (type == "clip") return if (reg1.containsMatchIn(url)) reg1.replace(url, "") else url
        return when {
            url.contains("%{width}", true) -> url.replace("%{width}", width).replace("%{height}", height)
            url.contains("{width}", true) -> url.replace("{width}", width).replace("{height}", height)
            reg2.containsMatchIn(url) -> reg2.replace(url, "${width}x${height}")
            reg3.containsMatchIn(url) -> reg3.replace(url, "${width}x${height}")
            reg4.containsMatchIn(url) -> reg4.replace(url, "${width}x${height}")
            reg5.containsMatchIn(url) -> reg5.replace(url, "${width}x${height}")
            reg6.containsMatchIn(url) -> reg6.replace(url, "${width}x${height}")
            else -> url
        }
    }

    fun getType(context: Context, type: String?): String? {
        return when (type?.lowercase()) {
            "archive" -> context.getString(R.string.video_type_archive)
            "highlight" -> context.getString(R.string.video_type_highlight)
            "upload" -> context.getString(R.string.video_type_upload)
            "rerun" -> context.getString(R.string.video_type_rerun)
            else -> null
        }
    }

    fun getUserType(context: Context, type: String?): String? {
        return when (type?.lowercase()) {
            "partner" -> context.getString(R.string.user_partner)
            "affiliate" -> context.getString(R.string.user_affiliate)
            "staff" -> context.getString(R.string.user_staff)
            "admin" -> context.getString(R.string.user_admin)
            "global_mod" -> context.getString(R.string.user_global_mod)
            else -> null
        }
    }

    fun getDuration(duration: String): Long {
        return try {
            parseLong(duration)
        } catch (e: NumberFormatException) {
            val h = duration.substringBefore("h", "0").takeLast(2).filter { it.isDigit() }.toInt()
            val m = duration.substringBefore("m", "0").takeLast(2).filter { it.isDigit() }.toInt()
            val s = duration.substringBefore("s", "0").takeLast(2).filter { it.isDigit() }.toInt()
            ((h * 3600) + (m * 60) + s).toLong()
        }
    }

    fun getDurationFromSeconds(context: Context, input: String?, text: Boolean = true): String? {
        if (input != null) {
            val duration = try {
                parseInt(input)
            } catch (e: NumberFormatException) {
                return null
            }
            val days = (duration / 86400)
            val hours = ((duration % 86400) / 3600)
            val minutes = (((duration % 86400) % 3600) / 60)
            val seconds = (duration % 60)
            return if (text) String.format((if (days != 0) (days.toString() + context.getString(R.string.days) + " ") else "") +
                    (if (hours != 0) (hours.toString() + context.getString(R.string.hours) + " ") else "") +
                    (if (minutes != 0) (minutes.toString() + context.getString(R.string.minutes) + " ") else "") +
                    (if (seconds != 0) (seconds.toString() + context.getString(R.string.seconds) + " ") else "")).trim() else
                String.format((if (days != 0) ("$days:") else "") +
                        (if (hours != 0) (if (hours < 10 && days != 0) "0$hours:" else "$hours:") else (if (days != 0) "00:" else "")) +
                        (if (minutes != 0) (if (minutes < 10 && (hours != 0||days != 0)) "0$minutes:" else "$minutes:") else (if (hours != 0||days != 0) "00:" else "")) +
                        (if (seconds != 0) (if (seconds < 10 && (minutes != 0||hours != 0||days != 0)) "0$seconds" else "$seconds") else (if (minutes != 0||hours != 0||days != 0) "00" else "")))
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

    fun getTimestamp(input: Long, timestampFormat: String?): String? {
        val pattern = when (timestampFormat) {
            "0" -> "H:mm"
            "1" -> "HH:mm"
            "2" -> "H:mm:ss"
            "3" -> "HH:mm:ss"
            "4" -> "h:mm a"
            "5" -> "hh:mm a"
            "6" -> "h:mm:ss a"
            else -> "hh:mm:ss a"
        }
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return try {
            format.format(Date(input))
        } catch (e: Exception) {
            null
        }
    }

    fun getClipTime(period: Period? = null): String {
        val days = when (period) {
            Period.DAY -> -1
            Period.WEEK -> -7
            Period.MONTH -> -30
            else -> 0 }
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(calendar.time)
    }

    fun parseIso8601Date(date: String): Long? {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(date)?.time
        } catch (e: ParseException) {
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault()).parse(date)?.time
            } catch (e: ParseException) {
                null
            }
        }
    }

    fun formatTimeString(context: Context, iso8601date: String): String? {
        return parseIso8601Date(iso8601date)?.let { formatTime(context, it) }
    }

    fun formatTime(context: Context, date: Long): String {
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

    fun startChat(channelName: String, userName: String?, userToken: String?, showUserNotice: Boolean, showClearMsg: Boolean, showClearChat: Boolean, newMessageListener: OnChatMessageReceivedListener, UserStateListener: OnUserStateReceivedListener, RoomStateListener: OnRoomStateReceivedListener, CommandListener: OnCommandReceivedListener): LiveChatThread {
        return LiveChatThread(userName, userToken, channelName, MessageListenerImpl(newMessageListener, UserStateListener, RoomStateListener, CommandListener, showUserNotice, showClearMsg, showClearChat)).apply { start() }
    }

    fun parseClipOffset(url: String): Double {
        val time = url.substringAfterLast('=').split("\\D".toRegex())
        var offset = 0.0
        var multiplier = 1.0
        for (i in time.lastIndex - 1 downTo 0) {
            offset += time[i].toDouble() * multiplier
            multiplier *= 60
        }
        return offset
    }

    fun addTokenPrefix(token: String) = "Bearer $token"

    fun formatViewsCount(context: Context, count: Int): String {
        return if (count > 1000 && context.prefs().getBoolean(C.UI_TRUNCATEVIEWCOUNT, false)) {
            context.getString(R.string.views, formatCountIfMoreThanAThousand(count))
        } else {
            context.resources.getQuantityString(R.plurals.views, count, count)
        }
    }

    fun formatViewersCount(context: Context, count: Int): String {
        return if (count > 1000 && context.prefs().getBoolean(C.UI_TRUNCATEVIEWCOUNT, false)) {
            context.getString(R.string.viewers, formatCountIfMoreThanAThousand(count))
        } else {
            context.resources.getQuantityString(R.plurals.viewers, count, count)
        }
    }

    fun formatCount(context: Context, count: Int): String {
        return if (count > 1000 && context.prefs().getBoolean(C.UI_TRUNCATEVIEWCOUNT, false)) {
            formatCountIfMoreThanAThousand(count)
        } else {
            count.toString()
        }
    }

    private fun formatCountIfMoreThanAThousand(count: Int): String {
        val divider: Int
        val suffix = if (count.toString().length < 7) {
            divider = 1000
            "K"
        } else {
            divider = 1_000_000
            "M"
        }
        val truncated = count / (divider / 10)
        val hasDecimal = truncated / 10.0 != (truncated / 10).toDouble()
        return if (hasDecimal) "${truncated / 10.0}$suffix" else "${truncated / 10}$suffix"
    }
}
