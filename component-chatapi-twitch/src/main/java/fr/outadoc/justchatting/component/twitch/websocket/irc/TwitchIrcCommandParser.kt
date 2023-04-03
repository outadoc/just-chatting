package fr.outadoc.justchatting.component.twitch.websocket.irc

import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.message.IrcMessageParser
import chat.willow.kale.irc.message.rfc1459.NoticeMessage
import chat.willow.kale.irc.message.rfc1459.PingMessage
import chat.willow.kale.irc.message.rfc1459.PrivMsgMessage
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.IrcEvent
import fr.outadoc.justchatting.utils.logging.logWarning
import kotlinx.datetime.Clock

class TwitchIrcCommandParser(private val clock: Clock) {

    fun parse(message: String): IrcEvent? {
        val ircMessage = IrcMessageParser.parse(message)
        val parsedMessage = when (ircMessage?.command) {
            PrivMsgMessage.command -> parseMessage(ircMessage)
            PingMessage.command -> IrcEvent.Command.Ping
            NoticeMessage.command -> parseNotice(ircMessage)
            "USERNOTICE" -> parseUserNotice(ircMessage)
            "USERSTATE" -> parseUserState(ircMessage)
            "CLEARMSG" -> parseClearMessage(ircMessage)
            "CLEARCHAT" -> parseClearChat(ircMessage)
            "ROOMSTATE" -> parseRoomState(ircMessage)
            else -> null
        }

        if (parsedMessage == null) {
            logWarning<TwitchIrcCommandParser> { "Unknown command: $message" }
        }

        return parsedMessage
    }

    private fun parseMessage(ircMessage: IrcMessage): IrcEvent.Message.ChatMessage? {
        val privateMessage = PrivMsgMessage.Message.Parser.parse(ircMessage)
            ?: return null

        // If the message is an action, it matches this regex, and we need
        // to extract the actual message contained inside
        val actionGroups = actionRegex.find(privateMessage.message)

        val message = actionGroups?.groupValues?.get(1) ?: privateMessage.message

        return IrcEvent.Message.ChatMessage(
            id = ircMessage.tags.id,
            userId = ircMessage.tags.userId,
            userLogin = ircMessage.tags.login ?: privateMessage.source.nick,
            userName = ircMessage.tags.displayName ?: privateMessage.source.nick,
            message = message,
            color = ircMessage.tags.color,
            isAction = actionGroups != null,
            embeddedEmotes = ircMessage.tags.parseEmotes(message),
            badges = ircMessage.tags.parseBadges(),
            isFirst = ircMessage.tags.firstMsg,
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
            rewardId = ircMessage.tags.customRewardId,
            inReplyTo = ircMessage.tags.parseParentMessage(),
            msgId = ircMessage.tags.messageId,
        )
    }

    private fun parseUserNotice(ircMessage: IrcMessage): IrcEvent.Message.UserNotice {
        return IrcEvent.Message.UserNotice(
            systemMsg = ircMessage.tags.systemMsg,
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
            userMessage = parseMessage(ircMessage),
            msgId = ircMessage.tags.messageId,
        )
    }

    private fun parseClearMessage(ircMessage: IrcMessage): IrcEvent {
        return IrcEvent.Command.ClearMessage(
            targetMessage = ircMessage.parameters.getOrNull(1),
            targetMessageId = ircMessage.tags.targetMessageId,
            targetUserLogin = ircMessage.tags.login,
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
        )
    }

    private fun parseClearChat(ircMessage: IrcMessage): IrcEvent {
        return IrcEvent.Command.ClearChat(
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
            targetUserId = ircMessage.tags.targetUserId,
            targetUserLogin = ircMessage.parameters.getOrNull(1),
            duration = ircMessage.tags.banDuration,
        )
    }

    private fun parseNotice(ircMessage: IrcMessage): IrcEvent.Message.Notice? {
        val notice = NoticeMessage.Command.Parser.parse(ircMessage)
            ?: return null

        return IrcEvent.Message.Notice(
            message = notice.message,
            messageId = ircMessage.tags.messageId,
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
        )
    }

    private fun parseRoomState(ircMessage: IrcMessage): IrcEvent.Command.RoomStateDelta {
        return IrcEvent.Command.RoomStateDelta(
            isEmoteOnly = ircMessage.tags.isEmoteOnly,
            minFollowDuration = ircMessage.tags.minFollowDuration,
            uniqueMessagesOnly = ircMessage.tags.uniqueMessagesOnly,
            slowModeDuration = ircMessage.tags.slowModeDuration,
            isSubOnly = ircMessage.tags.isSubOnly,
        )
    }

    private fun parseUserState(ircMessage: IrcMessage): IrcEvent.Command.UserState {
        return IrcEvent.Command.UserState(
            emoteSets = ircMessage.tags.emoteSets.orEmpty(),
        )
    }

    companion object {
        private val actionRegex = Regex("^\u0001ACTION (.+)\u0001\$")
    }
}
