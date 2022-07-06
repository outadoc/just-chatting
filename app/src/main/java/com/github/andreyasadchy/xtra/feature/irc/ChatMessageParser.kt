package com.github.andreyasadchy.xtra.feature.irc

import android.util.Log
import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.message.IrcMessageParser
import chat.willow.kale.irc.message.rfc1459.NoticeMessage
import chat.willow.kale.irc.message.rfc1459.PingMessage
import chat.willow.kale.irc.message.rfc1459.PrivMsgMessage
import com.github.andreyasadchy.xtra.model.chat.ChatCommand
import com.github.andreyasadchy.xtra.model.chat.Command
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.PingCommand
import com.github.andreyasadchy.xtra.model.chat.RoomState
import com.github.andreyasadchy.xtra.model.chat.UserState
import javax.inject.Inject

class ChatMessageParser @Inject constructor() {

    fun parse(message: String): ChatCommand? {
        val ircMessage = IrcMessageParser.parse(message) ?: return null
        return when (ircMessage.command) {
            PrivMsgMessage.command -> parseMessage(ircMessage)
            PingMessage.command -> PingCommand
            NoticeMessage.command -> parseNotice(ircMessage)
            "USERNOTICE" -> parseUserNotice(ircMessage)
            "USERSTATE" -> parseUserState(ircMessage)
            "CLEARMSG" -> parseClearMessage(ircMessage)
            "CLEARCHAT" -> parseClearChat(ircMessage)
            "ROOMSTATE" -> parseRoomState(ircMessage)
            else -> {
                Log.w(TAG, "Unknown command: $message")
                null
            }
        }
    }

    private fun parseMessage(ircMessage: IrcMessage): ChatCommand? {
        val privateMessage = PrivMsgMessage.Message.Parser.parse(ircMessage)
            ?: return null

        return LiveChatMessage(
            id = ircMessage.tags.id,
            userId = ircMessage.tags.userId,
            userLogin = ircMessage.tags.login ?: privateMessage.source.nick,
            userName = ircMessage.tags.displayName,
            message = privateMessage.message.removePrefix(ACTION_PREFIX),
            isAction = privateMessage.message.startsWith(ACTION_PREFIX),
            color = ircMessage.tags.color,
            rewardId = ircMessage.tags.customRewardId,
            isFirst = ircMessage.tags.firstMsg,
            msgId = ircMessage.tags.messageId,
            systemMsg = ircMessage.tags.systemMsg,
            emotes = ircMessage.tags.parseEmotes(),
            badges = ircMessage.tags.parseBadges(),
            timestamp = ircMessage.tags.parseTimestamp()
        )
    }

    private fun parseUserNotice(ircMessage: IrcMessage): Command.UserNotice? {
        val command = PrivMsgMessage.Command.Parser.parse(ircMessage)
            ?: return null

        return Command.UserNotice(
            message = ircMessage.tags.systemMsg ?: command.message,
            timestamp = ircMessage.tags.parseTimestamp()
        )
    }

    private fun parseClearMessage(ircMessage: IrcMessage): Command.ClearMessage {
        return Command.ClearMessage(
            message = ircMessage.tags.login,
            duration = ircMessage.parameters.getOrNull(1),
            timestamp = ircMessage.tags.parseTimestamp()
        )
    }

    private fun parseClearChat(ircMessage: IrcMessage): Command {
        val user = ircMessage.parameters.getOrNull(1)
        val duration = ircMessage.tags.banDuration

        return when {
            user == null ->
                Command.ClearChat(
                    duration = duration,
                    timestamp = ircMessage.tags.parseTimestamp()
                )
            duration != null ->
                Command.Timeout(
                    message = user,
                    duration = duration,
                    timestamp = ircMessage.tags.parseTimestamp()
                )
            else -> Command.Ban(
                message = user,
                timestamp = ircMessage.tags.parseTimestamp()
            )
        }
    }

    private fun parseNotice(ircMessage: IrcMessage): Command.Notice? {
        val notice = NoticeMessage.Command.Parser.parse(ircMessage)
            ?: return null

        return Command.Notice(
            message = notice.message,
            duration = ircMessage.tags.messageId
        )
    }

    private fun parseRoomState(ircMessage: IrcMessage): RoomState {
        return RoomState(
            emote = ircMessage.tags.isEmoteOnly,
            followers = ircMessage.tags.isFollowersOnly,
            unique = ircMessage.tags.isUniqueMode,
            slow = ircMessage.tags.isSlowMode,
            subs = ircMessage.tags.isSubOnly
        )
    }

    private fun parseUserState(ircMessage: IrcMessage): UserState {
        return UserState(emoteSets = ircMessage.tags.emoteSets)
    }

    companion object {
        private const val TAG = "ChatMessageParser"
        private const val ACTION_PREFIX = "\u0001ACTION"
    }
}
