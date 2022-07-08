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
        val ircMessage = IrcMessageParser.parse(message)
        val parsedMessage = when (ircMessage?.command) {
            PrivMsgMessage.command -> parseMessage(ircMessage)
            PingMessage.command -> PingCommand
            NoticeMessage.command -> parseNotice(ircMessage)
            "USERNOTICE" -> parseUserNotice(ircMessage)
            "USERSTATE" -> parseUserState(ircMessage)
            "CLEARMSG" -> parseClearMessage(ircMessage)
            "CLEARCHAT" -> parseClearChat(ircMessage)
            "ROOMSTATE" -> parseRoomState(ircMessage)
            else -> null
        }

        if (parsedMessage == null) {
            Log.w(TAG, "Unknown command: $message")
        }

        return parsedMessage
    }

    private fun parseMessage(ircMessage: IrcMessage): ChatCommand? {
        val privateMessage = PrivMsgMessage.Message.Parser.parse(ircMessage)
            ?: return null

        // If the message is an action, it matches this regex, and we need
        // to extract the actual message contained inside
        val actionGroups = actionRegex.find(privateMessage.message)

        return LiveChatMessage(
            id = ircMessage.tags.id,
            userId = ircMessage.tags.userId,
            userLogin = ircMessage.tags.login ?: privateMessage.source.nick,
            userName = ircMessage.tags.displayName,
            message = actionGroups?.groupValues?.get(1) ?: privateMessage.message,
            isAction = actionGroups != null,
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

    private fun parseUserNotice(ircMessage: IrcMessage): Command.UserNotice {
        return Command.UserNotice(
            message = ircMessage.tags.systemMsg ?: ircMessage.parameters.getOrNull(1),
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
            isEmoteOnly = ircMessage.tags.isEmoteOnly,
            minFollowDuration = ircMessage.tags.minFollowDuration,
            uniqueMessagesOnly = ircMessage.tags.uniqueMessagesOnly,
            slowModeDuration = ircMessage.tags.slowModeDuration,
            isSubOnly = ircMessage.tags.isSubOnly
        )
    }

    private fun parseUserState(ircMessage: IrcMessage): UserState {
        return UserState(emoteSets = ircMessage.tags.emoteSets)
    }

    companion object {
        private const val TAG = "ChatMessageParser"

        private val actionRegex = Regex("^\u0001ACTION (.+)\u0001\$")
    }
}
