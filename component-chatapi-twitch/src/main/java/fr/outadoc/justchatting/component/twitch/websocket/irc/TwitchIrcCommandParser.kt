package fr.outadoc.justchatting.component.twitch.websocket.irc

import chat.willow.kale.core.message.IrcMessage
import chat.willow.kale.irc.message.IrcMessageParser
import chat.willow.kale.irc.message.rfc1459.NoticeMessage
import chat.willow.kale.irc.message.rfc1459.PingMessage
import chat.willow.kale.irc.message.rfc1459.PrivMsgMessage
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.ChatMessage
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.Command
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.HostModeState
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.IrcEvent
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.PingCommand
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.RoomStateDelta
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.UserState
import fr.outadoc.justchatting.utils.logging.logWarning
import kotlinx.datetime.Clock

class TwitchIrcCommandParser(private val clock: Clock) {

    fun parse(message: String): IrcEvent? {
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
            "HOSTTARGET" -> parseHostTarget(ircMessage)
            else -> null
        }

        if (parsedMessage == null) {
            logWarning<TwitchIrcCommandParser> { "Unknown command: $message" }
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
            embeddedEmotes = ircMessage.tags.parseEmotes(message),
            badges = ircMessage.tags.parseBadges(),
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
            inReplyTo = ircMessage.tags.parseParentMessage(),
            msgId = ircMessage.tags.messageId,
            systemMsg = ircMessage.tags.systemMsg,
        )
    }

    private fun parseUserNotice(ircMessage: IrcMessage): Command.UserNotice {
        return Command.UserNotice(
            systemMsg = ircMessage.tags.systemMsg,
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
            userMessage = parseMessage(ircMessage),
            msgId = ircMessage.tags.messageId,
        )
    }

    private fun parseClearMessage(ircMessage: IrcMessage): Command.ClearMessage {
        return Command.ClearMessage(
            message = ircMessage.parameters.getOrNull(1),
            userLogin = ircMessage.tags.login,
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
        )
    }

    private fun parseClearChat(ircMessage: IrcMessage): Command {
        val user = ircMessage.parameters.getOrNull(1)
        val duration = ircMessage.tags.banDuration

        return when {
            user == null ->
                Command.ClearChat(
                    timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
                )

            duration != null ->
                Command.Timeout(
                    userLogin = user,
                    duration = duration,
                    timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
                )

            else -> Command.Ban(
                userLogin = user,
                timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
            )
        }
    }

    private fun parseNotice(ircMessage: IrcMessage): Command.Notice? {
        val notice = NoticeMessage.Command.Parser.parse(ircMessage)
            ?: return null

        return Command.Notice(
            message = notice.message,
            messageId = ircMessage.tags.messageId,
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
        )
    }

    private fun parseRoomState(ircMessage: IrcMessage): RoomStateDelta {
        return RoomStateDelta(
            isEmoteOnly = ircMessage.tags.isEmoteOnly,
            minFollowDuration = ircMessage.tags.minFollowDuration,
            uniqueMessagesOnly = ircMessage.tags.uniqueMessagesOnly,
            slowModeDuration = ircMessage.tags.slowModeDuration,
            isSubOnly = ircMessage.tags.isSubOnly,
        )
    }

    private fun parseHostTarget(ircMessage: IrcMessage): HostModeState? {
        val params = ircMessage.parameters.getOrNull(1)?.split(' ')
        if (params?.size != 2) return null

        val (targetLogin, viewerCount) = params
        return HostModeState(
            targetChannelLogin = targetLogin.takeIf { login -> login != "-" },
            viewerCount = viewerCount.toIntOrNull(),
        )
    }

    private fun parseUserState(ircMessage: IrcMessage): UserState {
        return UserState(emoteSets = ircMessage.tags.emoteSets.orEmpty())
    }

    companion object {
        private val actionRegex = Regex("^\u0001ACTION (.+)\u0001\$")
    }
}
