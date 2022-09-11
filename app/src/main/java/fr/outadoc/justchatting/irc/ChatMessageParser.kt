package fr.outadoc.justchatting.irc

import android.util.Log
import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.message.IrcMessageParser
import chat.willow.kale.irc.message.rfc1459.NoticeMessage
import chat.willow.kale.irc.message.rfc1459.PingMessage
import chat.willow.kale.irc.message.rfc1459.PrivMsgMessage
import fr.outadoc.justchatting.model.chat.ChatCommand
import fr.outadoc.justchatting.model.chat.Command
import fr.outadoc.justchatting.model.chat.ChatMessage
import fr.outadoc.justchatting.model.chat.PingCommand
import fr.outadoc.justchatting.model.chat.RoomState
import fr.outadoc.justchatting.model.chat.UserState

class ChatMessageParser {

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

    private fun parseMessage(ircMessage: IrcMessage): ChatMessage? {
        val privateMessage = PrivMsgMessage.Message.Parser.parse(ircMessage)
            ?: return null

        // If the message is an action, it matches this regex, and we need
        // to extract the actual message contained inside
        val actionGroups = actionRegex.find(privateMessage.message)

        val message = actionGroups?.groupValues?.get(1) ?: privateMessage.message

        return ChatMessage(
            id = ircMessage.tags.id,
            userId = ircMessage.tags.userId,
            userLogin = ircMessage.tags.login ?: privateMessage.source.nick,
            userName = ircMessage.tags.displayName ?: privateMessage.source.nick,
            message = message,
            isAction = actionGroups != null,
            color = ircMessage.tags.color,
            rewardId = ircMessage.tags.customRewardId,
            isFirst = ircMessage.tags.firstMsg,
            emotes = ircMessage.tags.parseEmotes(message),
            badges = ircMessage.tags.parseBadges(),
            timestamp = ircMessage.tags.parseTimestamp(),
            inReplyTo = ircMessage.tags.parseParentMessage(),
            msgId = ircMessage.tags.messageId,
            systemMsg = ircMessage.tags.systemMsg
        )
    }

    private fun parseUserNotice(ircMessage: IrcMessage): Command.UserNotice {
        return Command.UserNotice(
            systemMsg = ircMessage.tags.systemMsg,
            timestamp = ircMessage.tags.parseTimestamp(),
            userMessage = parseMessage(ircMessage),
            msgId = ircMessage.tags.messageId
        )
    }

    private fun parseClearMessage(ircMessage: IrcMessage): Command.ClearMessage {
        return Command.ClearMessage(
            message = ircMessage.parameters.getOrNull(1),
            userLogin = ircMessage.tags.login,
            timestamp = ircMessage.tags.parseTimestamp()
        )
    }

    private fun parseClearChat(ircMessage: IrcMessage): Command {
        val user = ircMessage.parameters.getOrNull(1)
        val duration = ircMessage.tags.banDuration

        return when {
            user == null ->
                Command.ClearChat(
                    timestamp = ircMessage.tags.parseTimestamp()
                )
            duration != null ->
                Command.Timeout(
                    userLogin = user,
                    duration = duration,
                    timestamp = ircMessage.tags.parseTimestamp()
                )
            else -> Command.Ban(
                userLogin = user,
                timestamp = ircMessage.tags.parseTimestamp()
            )
        }
    }

    private fun parseNotice(ircMessage: IrcMessage): Command.Notice? {
        val notice = NoticeMessage.Command.Parser.parse(ircMessage)
            ?: return null

        return Command.Notice(
            message = notice.message,
            messageId = ircMessage.tags.messageId,
            timestamp = ircMessage.tags.parseTimestamp()
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
        return UserState(emoteSets = ircMessage.tags.emoteSets.orEmpty())
    }

    companion object {
        private const val TAG = "ChatMessageParser"

        private val actionRegex = Regex("^\u0001ACTION (.+)\u0001\$")
    }
}
