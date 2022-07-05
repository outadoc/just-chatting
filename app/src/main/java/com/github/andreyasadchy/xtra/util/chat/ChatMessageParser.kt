package com.github.andreyasadchy.xtra.util.chat

import android.util.Log
import com.github.andreyasadchy.xtra.model.chat.Badge
import com.github.andreyasadchy.xtra.model.chat.ChatCommand
import com.github.andreyasadchy.xtra.model.chat.Command
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.PingCommand
import com.github.andreyasadchy.xtra.model.chat.RoomState
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.model.chat.UserState
import kotlinx.datetime.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ChatMessageParser @Inject constructor() {

    fun parse(message: String): ChatCommand? {
        return with(message) {
            when {
                contains("USERNOTICE") -> parseMessage(this, true)
                contains("NOTICE") -> parseNotice(this)
                contains("USERSTATE") -> parseUserState(this)
                contains("PRIVMSG") -> parseMessage(this, false)
                contains("CLEARMSG") -> parseClearMessage(this)
                contains("CLEARCHAT") -> parseClearChat(this)
                contains("ROOMSTATE") -> parseRoomState(this)
                startsWith("PING") -> PingCommand
                else -> {
                    Log.w(TAG, "Unknown command: $message")
                    null
                }
            }
        }
    }

    private fun parseMessage(message: String, userNotice: Boolean): ChatCommand {
        val parts = message.substring(1).split(" ", limit = 2)
        val prefix = parts[0]
        val prefixes = prefix.splitAndMakeMap(";", "=")
        val messageInfo = parts[1]

        // :<user>!<user>@<user>.tmi.twitch.tv PRIVMSG #<channelName> :<message>
        val userLogin = prefixes["login"] ?: try {
            messageInfo.substring(1, messageInfo.indexOf("!"))
        } catch (e: Exception) {
            null
        }

        val systemMsg = prefixes["system-msg"]?.replace("\\s", " ")
        val msgIndex = messageInfo.indexOf(
            " ",
            messageInfo.indexOf(
                "#",
                messageInfo.indexOf(":") + 1
            ) + 1
        )

        if (msgIndex == -1 && userNotice) {
            // no user message & is user notice
            return Command.UserNotice(
                message = systemMsg ?: messageInfo,
                timestamp = prefixes.parseTimestamp(),
                fullMsg = message
            )
        }

        val userMessage: String
        val isAction: Boolean

        messageInfo.substring(
            if (messageInfo.substring(msgIndex + 1).startsWith(":")) msgIndex + 2
            else msgIndex + 1
        ).let {
            // from <message>
            if (!it.startsWith("\u0001ACTION")) {
                userMessage = it
                isAction = false
            } else {
                userMessage = it.substring(8, it.lastIndex)
                isAction = true
            }
        }

        val emotesList: List<TwitchEmote>? =
            prefixes["emotes"]
                ?.splitAndMakeMap(split = "/", map = ":")
                ?.entries
                ?.flatMap { emote ->
                    emote.value
                        ?.split(",")
                        ?.map { indexes ->
                            val index = indexes.split("-")
                            TwitchEmote(
                                name = emote.key,
                                id = emote.key,
                                begin = index[0].toInt(),
                                end = index[1].toInt()
                            )
                        }
                        .orEmpty()
                }

        val badgesList: List<Badge>? =
            prefixes["badges"]
                ?.splitAndMakeMap(",", "/")
                ?.entries
                ?.mapNotNull { (key, value) ->
                    value?.let { Badge(key, value) }
                }

        return LiveChatMessage(
            id = prefixes["id"],
            userId = prefixes["user-id"],
            userLogin = userLogin,
            userName = prefixes["display-name"]?.replace("\\s", " "),
            message = userMessage,
            color = prefixes["color"],
            isAction = isAction,
            rewardId = prefixes["custom-reward-id"],
            isFirst = prefixes["first-msg"] == "1",
            msgId = prefixes["msg-id"],
            systemMsg = systemMsg,
            emotes = emotesList,
            badges = badgesList,
            timestamp = prefixes.parseTimestamp(),
            fullMsg = message
        )
    }

    private fun parseClearMessage(message: String): Command.ClearMessage {
        val parts = message.substring(1).split(" ", limit = 2)
        val prefix = parts[0]
        val prefixes = prefix.splitAndMakeMap(";", "=")
        val user = prefixes["login"]
        val messageInfo = parts[1]
        val msgIndex = messageInfo.indexOf(":", messageInfo.indexOf(":") + 1)
        val msg = if (msgIndex != -1) messageInfo.substring(msgIndex + 1) else null

        return Command.ClearMessage(
            message = user,
            duration = msg,
            type = "clearmsg",
            timestamp = prefixes.parseTimestamp(),
            fullMsg = message
        )
    }

    private fun parseClearChat(message: String): Command {
        val parts = message.substring(1).split(" ", limit = 2)
        val prefix = parts[0]
        val prefixes = prefix.splitAndMakeMap(";", "=")
        val duration = prefixes["ban-duration"]
        val messageInfo = parts[1]
        val userIndex = messageInfo.indexOf(":", messageInfo.indexOf(":") + 1)
        val user = if (userIndex != -1) messageInfo.substring(userIndex + 1) else null

        return when {
            user == null ->
                Command.ClearChat(
                    duration = duration,
                    type = "clearchat",
                    timestamp = prefixes.parseTimestamp(),
                    fullMsg = message
                )
            duration != null ->
                Command.Timeout(
                    message = user,
                    duration = duration,
                    type = "timeout",
                    timestamp = prefixes.parseTimestamp(),
                    fullMsg = message
                )
            else -> Command.Ban(
                message = user,
                type = "ban",
                timestamp = prefixes.parseTimestamp(),
                fullMsg = message
            )
        }
    }

    private fun parseNotice(message: String): Command.Notice {
        val parts = message.substring(1).split(" ", limit = 2)
        val prefix = parts[0]
        val prefixes = prefix.splitAndMakeMap(";", "=")
        val messageInfo = parts[1]
        val msgId = prefixes["msg-id"]
        return Command.Notice(
            message = messageInfo.substring(
                messageInfo.indexOf(
                    ":",
                    messageInfo.indexOf(":") + 1
                ) + 1
            ),
            duration = msgId,
            type = "notice",
            fullMsg = message
        )
    }

    private fun parseRoomState(message: String): RoomState {
        val parts = message.substring(1).split(" ", limit = 2)
        val prefix = parts[0]
        val prefixes = prefix.splitAndMakeMap(";", "=")

        return RoomState(
            emote = prefixes["emote-only"] == "1",
            followers = prefixes["followers-only"]?.toInt()?.takeUnless { it == -1 }?.minutes,
            unique = prefixes["r9k"] == "1",
            slow = prefixes["slow"]?.toInt()?.seconds,
            subs = prefixes["subs-only"] == "1"
        )
    }

    private fun parseUserState(message: String): UserState {
        val parts = message.substring(1).split(' ', limit = 2)
        val prefix = parts[0]
        val prefixes = prefix.splitAndMakeMap(";", "=")
        return UserState(
            emoteSets = prefixes["emote-sets"]
                ?.split(",")
                ?.dropLastWhile { it.isEmpty() }
        )
    }

    private fun String.splitAndMakeMap(
        split: String,
        map: String
    ): Map<String, String?> = buildMap {
        this@splitAndMakeMap.split(split)
            .dropLastWhile { it.isEmpty() }
            .asSequence()
            .map { pair -> pair.split(map).dropLastWhile { it.isEmpty() } }
            .forEach { this[it[0]] = if (it.size == 2) it[1] else null }
    }

    private fun Map<String, String?>.parseTimestamp(): Instant? {
        val prop = this["tmi-sent-ts"] ?: this["rm-received-ts"]
        return prop?.toLong()?.let { Instant.fromEpochMilliseconds(it) }
    }

    companion object {
        private const val TAG = "ChatMessageParser"
    }
}