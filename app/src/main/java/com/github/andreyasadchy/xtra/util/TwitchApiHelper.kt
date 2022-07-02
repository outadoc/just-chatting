package com.github.andreyasadchy.xtra.util

import android.content.Context
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.util.chat.LiveChatThread
import com.github.andreyasadchy.xtra.util.chat.LoggedInChatThread
import com.github.andreyasadchy.xtra.util.chat.MessageListenerImpl
import com.github.andreyasadchy.xtra.util.chat.OnChatMessageReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnCommandReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnRewardReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnRoomStateReceivedListener
import com.github.andreyasadchy.xtra.util.chat.OnUserStateReceivedListener
import com.github.andreyasadchy.xtra.util.chat.PubSubListenerImpl
import com.github.andreyasadchy.xtra.util.chat.PubSubWebSocket
import kotlinx.coroutines.CoroutineScope

object TwitchApiHelper {

    var checkedValidation = false

    fun getTemplateUrl(url: String?, type: String): String? {
        if (url.isNullOrEmpty() || url.startsWith("https://vod-secure.twitch.tv/_404/404_processing"))
            return when (type) {
                "game" -> "https://static-cdn.jtvnw.net/ttv-static/404_boxart.jpg"
                "video" -> "https://vod-secure.twitch.tv/_404/404_processing_320x180.png"
                else -> null
            }

        val width = when (type) {
            "game" -> "285"
            "video" -> "1280"
            "profileimage" -> "300"
            else -> ""
        }

        val height = when (type) {
            "game" -> "380"
            "video" -> "720"
            "profileimage" -> "300"
            else -> ""
        }

        val reg1 = """-\d\d\dx\d\d\d""".toRegex()
        val reg2 = """\d\d\d\dx\d\d\d""".toRegex()
        val reg3 = """\d\d\dx\d\d\d""".toRegex()
        val reg4 = """\d\dx\d\d\d""".toRegex()
        val reg5 = """\d\d\dx\d\d""".toRegex()
        val reg6 = """\d\dx\d\d""".toRegex()

        if (type == "clip") {
            return if (reg1.containsMatchIn(url)) {
                reg1.replace(url, "")
            } else {
                url
            }
        }

        return when {
            url.contains("%{width}", true) -> url.replace("%{width}", width)
                .replace("%{height}", height)
            url.contains("{width}", true) -> url.replace("{width}", width)
                .replace("{height}", height)
            reg2.containsMatchIn(url) -> reg2.replace(url, "${width}x$height")
            reg3.containsMatchIn(url) -> reg3.replace(url, "${width}x$height")
            reg4.containsMatchIn(url) -> reg4.replace(url, "${width}x$height")
            reg5.containsMatchIn(url) -> reg5.replace(url, "${width}x$height")
            reg6.containsMatchIn(url) -> reg6.replace(url, "${width}x$height")
            else -> url
        }
    }

    fun startChat(
        loggedIn: Boolean,
        channelName: String,
        showUserNotice: Boolean,
        showClearMsg: Boolean,
        showClearChat: Boolean,
        usePubSub: Boolean,
        newMessageListener: OnChatMessageReceivedListener,
        UserStateListener: OnUserStateReceivedListener,
        RoomStateListener: OnRoomStateReceivedListener,
        CommandListener: OnCommandReceivedListener,
        callbackReward: OnRewardReceivedListener
    ): LiveChatThread {
        return LiveChatThread(
            loggedIn = loggedIn,
            channelName = channelName,
            listener = MessageListenerImpl(
                callback = newMessageListener,
                callbackUserState = UserStateListener,
                callbackRoomState = RoomStateListener,
                callbackCommand = CommandListener,
                callbackReward = callbackReward,
                showUserNotice = showUserNotice,
                showClearMsg = showClearMsg,
                showClearChat = showClearChat,
                usePubSub = usePubSub
            )
        ).apply { start() }
    }

    fun startLoggedInChat(
        userName: String?,
        userToken: String?,
        channelName: String,
        showUserNotice: Boolean,
        showClearMsg: Boolean,
        showClearChat: Boolean,
        usePubSub: Boolean,
        newMessageListener: OnChatMessageReceivedListener,
        UserStateListener: OnUserStateReceivedListener,
        RoomStateListener: OnRoomStateReceivedListener,
        CommandListener: OnCommandReceivedListener,
        callbackReward: OnRewardReceivedListener
    ): LoggedInChatThread {
        return LoggedInChatThread(
            userLogin = userName,
            userToken = userToken,
            channelName = channelName,
            listener = MessageListenerImpl(
                callback = newMessageListener,
                callbackUserState = UserStateListener,
                callbackRoomState = RoomStateListener,
                callbackCommand = CommandListener,
                callbackReward = callbackReward,
                showUserNotice = showUserNotice,
                showClearMsg = showClearMsg,
                showClearChat = showClearChat,
                usePubSub = usePubSub
            )
        ).apply { start() }
    }

    fun startPubSub(
        channelId: String,
        coroutineScope: CoroutineScope,
        newMessageListener: OnChatMessageReceivedListener,
        callbackReward: OnRewardReceivedListener
    ): PubSubWebSocket {
        return PubSubWebSocket(
            channelId,
            coroutineScope,
            PubSubListenerImpl(newMessageListener, callbackReward)
        ).apply { connect() }
    }

    fun addTokenPrefixHelix(token: String) = "Bearer $token"
    fun addTokenPrefixGQL(token: String) = "OAuth $token"

    fun formatViewersCount(context: Context, count: Int): String {
        return context.resources.getQuantityString(R.plurals.viewers, count, "%,d".format(count))
    }

    fun formatCount(count: Int): String {
        return "%,d".format(count)
    }

    fun getMessageIdString(context: Context, msgId: String?): String? {
        return when (msgId) {
            "highlighted-message" -> context.getString(R.string.irc_msgid_highlighted_message)
            "announcement" -> context.getString(R.string.irc_msgid_announcement)
            else -> null
        }
    }

    fun getNoticeString(context: Context, msgId: String?, message: String?): String? {
        return when (msgId) {
            "already_banned" -> context.getString(
                R.string.irc_notice_already_banned,
                message?.substringBefore(" is already banned", "") ?: ""
            )
            "already_emote_only_off" -> context.getString(R.string.irc_notice_already_emote_only_off)
            "already_emote_only_on" -> context.getString(R.string.irc_notice_already_emote_only_on)
            "already_followers_off" -> context.getString(R.string.irc_notice_already_followers_off)
            "already_followers_on" -> context.getString(
                R.string.irc_notice_already_followers_on,
                message?.substringAfter("is already in ", "")
                    ?.substringBefore(" followers-only mode", "") ?: ""
            )
            "already_r9k_off" -> context.getString(R.string.irc_notice_already_r9k_off)
            "already_r9k_on" -> context.getString(R.string.irc_notice_already_r9k_on)
            "already_slow_off" -> context.getString(R.string.irc_notice_already_slow_off)
            "already_slow_on" -> context.getString(
                R.string.irc_notice_already_slow_on,
                message?.substringAfter("is already in ", "")?.substringBefore("-second slow", "")
                    ?: ""
            )
            "already_subs_off" -> context.getString(R.string.irc_notice_already_subs_off)
            "already_subs_on" -> context.getString(R.string.irc_notice_already_subs_on)
            "autohost_receive" -> context.getString(
                R.string.irc_notice_autohost_receive,
                message?.substringBefore(" is now auto hosting", "") ?: "",
                message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "") ?: ""
            )
            "bad_ban_admin" -> context.getString(
                R.string.irc_notice_bad_ban_admin,
                message?.substringAfter("cannot ban admin", "")
                    ?.substringBefore(". Please email", "") ?: ""
            )
            "bad_ban_anon" -> context.getString(R.string.irc_notice_bad_ban_anon)
            "bad_ban_broadcaster" -> context.getString(R.string.irc_notice_bad_ban_broadcaster)
            "bad_ban_mod" -> context.getString(
                R.string.irc_notice_bad_ban_mod,
                message?.substringAfter("cannot ban moderator", "")
                    ?.substringBefore(" unless you are", "") ?: ""
            )
            "bad_ban_self" -> context.getString(R.string.irc_notice_bad_ban_self)
            "bad_ban_staff" -> context.getString(
                R.string.irc_notice_bad_ban_staff,
                message?.substringAfter("cannot ban staff", "")
                    ?.substringBefore(". Please email", "") ?: ""
            )
            "bad_commercial_error" -> context.getString(R.string.irc_notice_bad_commercial_error)
            "bad_delete_message_broadcaster" -> context.getString(R.string.irc_notice_bad_delete_message_broadcaster)
            "bad_delete_message_mod" -> context.getString(
                R.string.irc_notice_bad_delete_message_mod,
                message?.substringAfter("from another moderator ", "")?.substringBeforeLast(".", "")
                    ?: ""
            )
            "bad_host_error" -> context.getString(
                R.string.irc_notice_bad_host_error,
                message?.substringAfter("a problem hosting ", "")
                    ?.substringBefore(". Please try", "") ?: ""
            )
            "bad_host_hosting" -> context.getString(
                R.string.irc_notice_bad_host_hosting,
                message?.substringAfter("is already hosting ", "")?.substringBeforeLast(".", "")
                    ?: ""
            )
            "bad_host_rate_exceeded" -> context.getString(
                R.string.irc_notice_bad_host_rate_exceeded,
                message?.substringAfter("changed more than ", "")
                    ?.substringBefore(" times every half", "") ?: ""
            )
            "bad_host_rejected" -> context.getString(R.string.irc_notice_bad_host_rejected)
            "bad_host_self" -> context.getString(R.string.irc_notice_bad_host_self)
            "bad_mod_banned" -> context.getString(
                R.string.irc_notice_bad_mod_banned,
                message?.substringBefore(" is banned", "") ?: ""
            )
            "bad_mod_mod" -> context.getString(
                R.string.irc_notice_bad_mod_mod,
                message?.substringBefore(" is already", "") ?: ""
            )
            "bad_slow_duration" -> context.getString(
                R.string.irc_notice_bad_slow_duration,
                message?.substringAfter("to more than ", "")?.substringBefore(" seconds.", "") ?: ""
            )
            "bad_timeout_admin" -> context.getString(
                R.string.irc_notice_bad_timeout_admin,
                message?.substringAfter("cannot timeout admin ", "")
                    ?.substringBefore(". Please email", "") ?: ""
            )
            "bad_timeout_anon" -> context.getString(R.string.irc_notice_bad_timeout_anon)
            "bad_timeout_broadcaster" -> context.getString(R.string.irc_notice_bad_timeout_broadcaster)
            "bad_timeout_duration" -> context.getString(
                R.string.irc_notice_bad_timeout_duration,
                message?.substringAfter("for more than ", "")?.substringBeforeLast(".", "") ?: ""
            )
            "bad_timeout_mod" -> context.getString(
                R.string.irc_notice_bad_timeout_mod,
                message?.substringAfter("cannot timeout moderator ", "")
                    ?.substringBefore(" unless you are", "") ?: ""
            )
            "bad_timeout_self" -> context.getString(R.string.irc_notice_bad_timeout_self)
            "bad_timeout_staff" -> context.getString(
                R.string.irc_notice_bad_timeout_staff,
                message?.substringAfter("cannot timeout staff ", "")
                    ?.substringBefore(". Please email", "") ?: ""
            )
            "bad_unban_no_ban" -> context.getString(
                R.string.irc_notice_bad_unban_no_ban,
                message?.substringBefore(" is not banned", "") ?: ""
            )
            "bad_unhost_error" -> context.getString(R.string.irc_notice_bad_unhost_error)
            "bad_unmod_mod" -> context.getString(
                R.string.irc_notice_bad_unmod_mod,
                message?.substringBefore(" is not a", "") ?: ""
            )
            "bad_vip_grantee_banned" -> context.getString(
                R.string.irc_notice_bad_vip_grantee_banned,
                message?.substringBefore(" is banned in", "") ?: ""
            )
            "bad_vip_grantee_already_vip" -> context.getString(
                R.string.irc_notice_bad_vip_grantee_already_vip,
                message?.substringBefore(" is already a", "") ?: ""
            )
            "bad_vip_max_vips_reached" -> context.getString(R.string.irc_notice_bad_vip_max_vips_reached)
            "bad_vip_achievement_incomplete" -> context.getString(R.string.irc_notice_bad_vip_achievement_incomplete)
            "bad_unvip_grantee_not_vip" -> context.getString(
                R.string.irc_notice_bad_unvip_grantee_not_vip,
                message?.substringBefore(" is not a", "") ?: ""
            )
            "ban_success" -> context.getString(
                R.string.irc_notice_ban_success,
                message?.substringBefore(" is now banned", "") ?: ""
            )
            "cmds_available" -> context.getString(
                R.string.irc_notice_cmds_available,
                message?.substringAfter("details): ", "")?.substringBefore(" More help:", "") ?: ""
            )
            "color_changed" -> context.getString(R.string.irc_notice_color_changed)
            "commercial_success" -> context.getString(
                R.string.irc_notice_commercial_success,
                message?.substringAfter("Initiating ", "")
                    ?.substringBefore(" second commercial break.", "") ?: ""
            )
            "delete_message_success" -> context.getString(
                R.string.irc_notice_delete_message_success,
                message?.substringAfter("The message from ", "")
                    ?.substringBefore(" is now deleted.", "") ?: ""
            )
            "delete_staff_message_success" -> context.getString(
                R.string.irc_notice_delete_staff_message_success,
                message?.substringAfter("message from staff ", "")
                    ?.substringBefore(". Please email", "") ?: ""
            )
            "emote_only_off" -> context.getString(R.string.irc_notice_emote_only_off)
            "emote_only_on" -> context.getString(R.string.irc_notice_emote_only_on)
            "followers_off" -> context.getString(R.string.irc_notice_followers_off)
            "followers_on" -> context.getString(
                R.string.irc_notice_followers_on,
                message?.substringAfter("is now in ", "")
                    ?.substringBefore(" followers-only mode", "") ?: ""
            )
            "followers_on_zero" -> context.getString(R.string.irc_notice_followers_on_zero)
            "host_off" -> context.getString(R.string.irc_notice_host_off)
            "host_on" -> context.getString(
                R.string.irc_notice_host_on,
                message?.substringAfter("Now hosting ", "")?.substringBeforeLast(".", "") ?: ""
            )
            "host_receive" -> context.getString(
                R.string.irc_notice_host_receive,
                message?.substringBefore(" is now hosting", "") ?: "",
                message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "") ?: ""
            )
            "host_receive_no_count" -> context.getString(
                R.string.irc_notice_host_receive_no_count,
                message?.substringBefore(" is now hosting", "") ?: ""
            )
            "host_target_went_offline" -> context.getString(
                R.string.irc_notice_host_target_went_offline,
                message?.substringBefore(" has gone offline", "") ?: ""
            )
            "hosts_remaining" -> context.getString(
                R.string.irc_notice_hosts_remaining,
                message?.substringBefore(" host commands", "") ?: ""
            )
            "invalid_user" -> context.getString(
                R.string.irc_notice_invalid_user,
                message?.substringAfter("Invalid username: ", "") ?: ""
            )
            "mod_success" -> context.getString(
                R.string.irc_notice_mod_success,
                message?.substringAfter("You have added ", "")
                    ?.substringBefore(" as a moderator", "") ?: ""
            )
            "msg_banned" -> context.getString(
                R.string.irc_notice_msg_banned,
                message?.substringAfter("from talking in ", "")?.substringBeforeLast(".", "") ?: ""
            )
            "msg_bad_characters" -> context.getString(R.string.irc_notice_msg_bad_characters)
            "msg_channel_blocked" -> context.getString(R.string.irc_notice_msg_channel_blocked)
            "msg_channel_suspended" -> context.getString(R.string.irc_notice_msg_channel_suspended)
            "msg_duplicate" -> context.getString(R.string.irc_notice_msg_duplicate)
            "msg_emoteonly" -> context.getString(R.string.irc_notice_msg_emoteonly)
            "msg_followersonly" -> context.getString(
                R.string.irc_notice_msg_followersonly,
                message?.substringAfter("This room is in ", "")
                    ?.substringBefore(" followers-only mode", "") ?: "",
                message?.substringAfter("Follow ", "")?.substringBefore(" to join", "") ?: ""
            )
            "msg_followersonly_followed" -> context.getString(
                R.string.irc_notice_msg_followersonly_followed,
                message?.substringAfter("This room is in ", "")
                    ?.substringBefore(" followers-only mode", "") ?: "",
                message?.substringAfter("following for ", "")?.substringBefore(". Continue", "")
                    ?: ""
            )
            "msg_followersonly_zero" -> context.getString(
                R.string.irc_notice_msg_followersonly_zero,
                message?.substringAfter(". Follow ", "")?.substringBefore(" to join the", "") ?: ""
            )
            "msg_r9k" -> context.getString(R.string.irc_notice_msg_r9k)
            "msg_ratelimit" -> context.getString(R.string.irc_notice_msg_ratelimit)
            "msg_rejected" -> context.getString(R.string.irc_notice_msg_rejected)
            "msg_rejected_mandatory" -> context.getString(R.string.irc_notice_msg_rejected_mandatory)
            "msg_slowmode" -> context.getString(
                R.string.irc_notice_msg_slowmode,
                message?.substringAfter("talk again in ", "")?.substringBefore(" seconds.", "")
                    ?: ""
            )
            "msg_subsonly" -> context.getString(
                R.string.irc_notice_msg_subsonly,
                message?.substringAfter("/products/", "")?.substringBefore("/ticket?ref", "") ?: ""
            )
            "msg_suspended" -> context.getString(R.string.irc_notice_msg_suspended)
            "msg_timedout" -> context.getString(
                R.string.irc_notice_msg_timedout,
                message?.substringAfter("timed out for ", "")?.substringBefore(" more seconds.", "")
                    ?: ""
            )
            "msg_verified_email" -> context.getString(R.string.irc_notice_msg_verified_email)
            "no_help" -> context.getString(R.string.irc_notice_no_help)
            "no_mods" -> context.getString(R.string.irc_notice_no_mods)
            "no_vips" -> context.getString(R.string.irc_notice_no_vips)
            "not_hosting" -> context.getString(R.string.irc_notice_not_hosting)
            "no_permission" -> context.getString(R.string.irc_notice_no_permission)
            "r9k_off" -> context.getString(R.string.irc_notice_r9k_off)
            "r9k_on" -> context.getString(R.string.irc_notice_r9k_on)
            "raid_error_already_raiding" -> context.getString(R.string.irc_notice_raid_error_already_raiding)
            "raid_error_forbidden" -> context.getString(R.string.irc_notice_raid_error_forbidden)
            "raid_error_self" -> context.getString(R.string.irc_notice_raid_error_self)
            "raid_error_too_many_viewers" -> context.getString(R.string.irc_notice_raid_error_too_many_viewers)
            "raid_error_unexpected" -> context.getString(
                R.string.irc_notice_raid_error_unexpected,
                message?.substringAfter("a problem raiding ", "")
                    ?.substringBefore(". Please try", "") ?: ""
            )
            "raid_notice_mature" -> context.getString(R.string.irc_notice_raid_notice_mature)
            "raid_notice_restricted_chat" -> context.getString(R.string.irc_notice_raid_notice_restricted_chat)
            "room_mods" -> context.getString(
                R.string.irc_notice_room_mods,
                message?.substringAfter("this channel are: ", "") ?: ""
            )
            "slow_off" -> context.getString(R.string.irc_notice_slow_off)
            "slow_on" -> context.getString(
                R.string.irc_notice_slow_on,
                message?.substringAfter("send messages every ", "")
                    ?.substringBefore(" seconds.", "") ?: ""
            )
            "subs_off" -> context.getString(R.string.irc_notice_subs_off)
            "subs_on" -> context.getString(R.string.irc_notice_subs_on)
            "timeout_no_timeout" -> context.getString(
                R.string.irc_notice_timeout_no_timeout,
                message?.substringBefore(" is not timed", "") ?: ""
            )
            "timeout_success" -> context.getString(
                R.string.irc_notice_timeout_success,
                message?.substringBefore(" has been", "") ?: "",
                message?.substringAfter("timed out for ", "")?.substringBeforeLast(".", "") ?: ""
            )
            "tos_ban" -> context.getString(
                R.string.irc_notice_tos_ban,
                message?.substringAfter("has closed channel ", "")
                    ?.substringBefore(" due to Terms", "") ?: ""
            )
            "turbo_only_color" -> context.getString(
                R.string.irc_notice_turbo_only_color,
                message?.substringAfter("following instead: ", "") ?: ""
            )
            "unavailable_command" -> context.getString(
                R.string.irc_notice_unavailable_command,
                message?.substringAfter("Sorry, “", "")?.substringBefore("” is not available", "")
                    ?: ""
            )
            "unban_success" -> context.getString(
                R.string.irc_notice_unban_success,
                message?.substringBefore(" is no longer", "") ?: ""
            )
            "unmod_success" -> context.getString(
                R.string.irc_notice_unmod_success,
                message?.substringAfter("You have removed ", "")
                    ?.substringBefore(" as a moderator", "") ?: ""
            )
            "unraid_error_no_active_raid" -> context.getString(R.string.irc_notice_unraid_error_no_active_raid)
            "unraid_error_unexpected" -> context.getString(R.string.irc_notice_unraid_error_unexpected)
            "unraid_success" -> context.getString(R.string.irc_notice_unraid_success)
            "unrecognized_cmd" -> context.getString(
                R.string.irc_notice_unrecognized_cmd,
                message?.substringAfter("Unrecognized command: ", "") ?: ""
            )
            "untimeout_banned" -> context.getString(
                R.string.irc_notice_untimeout_banned,
                message?.substringBefore(" is permanently banned", "") ?: ""
            )
            "untimeout_success" -> context.getString(
                R.string.irc_notice_untimeout_success,
                message?.substringBefore(" is no longer", "") ?: ""
            )
            "unvip_success" -> context.getString(
                R.string.irc_notice_unvip_success,
                message?.substringAfter("You have removed ", "")?.substringBefore(" as a VIP", "")
                    ?: ""
            )
            "usage_ban" -> context.getString(R.string.irc_notice_usage_ban)
            "usage_clear" -> context.getString(R.string.irc_notice_usage_clear)
            "usage_color" -> context.getString(
                R.string.irc_notice_usage_color,
                message?.substringAfter("following: ", "")?.substringBeforeLast(".", "") ?: ""
            )
            "usage_commercial" -> context.getString(R.string.irc_notice_usage_commercial)
            "usage_disconnect" -> context.getString(R.string.irc_notice_usage_disconnect)
            "usage_delete" -> context.getString(R.string.irc_notice_usage_delete)
            "usage_emote_only_off" -> context.getString(R.string.irc_notice_usage_emote_only_off)
            "usage_emote_only_on" -> context.getString(R.string.irc_notice_usage_emote_only_on)
            "usage_followers_off" -> context.getString(R.string.irc_notice_usage_followers_off)
            "usage_followers_on" -> context.getString(R.string.irc_notice_usage_followers_on)
            "usage_help" -> context.getString(R.string.irc_notice_usage_help)
            "usage_host" -> context.getString(R.string.irc_notice_usage_host)
            "usage_marker" -> context.getString(R.string.irc_notice_usage_marker)
            "usage_me" -> context.getString(R.string.irc_notice_usage_me)
            "usage_mod" -> context.getString(R.string.irc_notice_usage_mod)
            "usage_mods" -> context.getString(R.string.irc_notice_usage_mods)
            "usage_r9k_off" -> context.getString(R.string.irc_notice_usage_r9k_off)
            "usage_r9k_on" -> context.getString(R.string.irc_notice_usage_r9k_on)
            "usage_raid" -> context.getString(R.string.irc_notice_usage_raid)
            "usage_slow_off" -> context.getString(R.string.irc_notice_usage_slow_off)
            "usage_slow_on" -> context.getString(
                R.string.irc_notice_usage_slow_on,
                message?.substringAfter("default=", "")?.substringBefore(")", "") ?: ""
            )
            "usage_subs_off" -> context.getString(R.string.irc_notice_usage_subs_off)
            "usage_subs_on" -> context.getString(R.string.irc_notice_usage_subs_on)
            "usage_timeout" -> context.getString(R.string.irc_notice_usage_timeout)
            "usage_unban" -> context.getString(R.string.irc_notice_usage_unban)
            "usage_unhost" -> context.getString(R.string.irc_notice_usage_unhost)
            "usage_unmod" -> context.getString(R.string.irc_notice_usage_unmod)
            "usage_unraid" -> context.getString(R.string.irc_notice_usage_unraid)
            "usage_untimeout" -> context.getString(R.string.irc_notice_usage_untimeout)
            "usage_unvip" -> context.getString(R.string.irc_notice_usage_unvip)
            "usage_user" -> context.getString(R.string.irc_notice_usage_user)
            "usage_vip" -> context.getString(R.string.irc_notice_usage_vip)
            "usage_vips" -> context.getString(R.string.irc_notice_usage_vips)
            "usage_whisper" -> context.getString(R.string.irc_notice_usage_whisper)
            "vip_success" -> context.getString(
                R.string.irc_notice_vip_success,
                message?.substringAfter("You have added ", "")?.substringBeforeLast(" as a vip", "")
                    ?: ""
            )
            "vips_success" -> context.getString(
                R.string.irc_notice_vips_success,
                message?.substringAfter("channel are: ", "")?.substringBeforeLast(".", "") ?: ""
            )
            "whisper_banned" -> context.getString(R.string.irc_notice_whisper_banned)
            "whisper_banned_recipient" -> context.getString(R.string.irc_notice_whisper_banned_recipient)
            "whisper_invalid_login" -> context.getString(R.string.irc_notice_whisper_invalid_login)
            "whisper_invalid_self" -> context.getString(R.string.irc_notice_whisper_invalid_self)
            "whisper_limit_per_min" -> context.getString(R.string.irc_notice_whisper_limit_per_min)
            "whisper_limit_per_sec" -> context.getString(R.string.irc_notice_whisper_limit_per_sec)
            "whisper_restricted" -> context.getString(R.string.irc_notice_whisper_restricted)
            "whisper_restricted_recipient" -> context.getString(R.string.irc_notice_whisper_restricted_recipient)
            else -> null
        }
    }
}
