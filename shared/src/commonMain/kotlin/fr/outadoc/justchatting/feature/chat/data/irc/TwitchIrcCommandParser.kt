package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.data.irc.parser.core.message.IrcMessage
import fr.outadoc.justchatting.feature.chat.data.irc.parser.irc.message.IrcMessageParser
import fr.outadoc.justchatting.feature.chat.data.irc.parser.irc.message.rfc1459.NoticeMessage
import fr.outadoc.justchatting.feature.chat.data.irc.parser.irc.message.rfc1459.PrivMsgMessage
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logWarning
import kotlin.time.Clock

internal class TwitchIrcCommandParser(
    private val clock: Clock,
) {
    /**
     * @param commandsOfInterest If non-null, restricts parsing to these IRC commands (plus
     * PING, always handled). Every other command is skipped before the (relatively expensive)
     * per-command field extraction runs, and yields `null` instead of an "Unknown command"
     * warning. Lets a caller that only reads a subset of commands (e.g.
     * [fr.outadoc.justchatting.feature.chat.data.irc.LoggedInChatWebSocket], which only cares
     * about NOTICE/USERSTATE) skip the cost of fully parsing every PRIVMSG - by far the most
     * common command on a busy channel - just to discard it.
     */
    fun parse(
        message: String,
        commandsOfInterest: Set<String>? = null,
    ): ChatEvent? =
        try {
            parseInternal(message, commandsOfInterest)
        } catch (e: Exception) {
            // A malformed line must never propagate: an exception thrown here would
            // close the chat socket, and recent-message backfill would replay the same
            // line on every reconnection.
            logError<TwitchIrcCommandParser>(e) { "Failed to parse message: $message" }
            null
        }

    private fun parseInternal(
        message: String,
        commandsOfInterest: Set<String>?,
    ): ChatEvent? {
        val ircMessage = IrcMessageParser.parse(message)
        if (ircMessage == null) {
            logWarning<TwitchIrcCommandParser> { "Unknown command: $message" }
            return null
        }

        if (ircMessage.command == "PING") {
            return ChatEvent.Command.Ping
        }

        if (commandsOfInterest != null && ircMessage.command !in commandsOfInterest) {
            return null
        }

        val parsedMessage =
            when (ircMessage.command) {
                "PRIVMSG" -> parsePrivateMsg(ircMessage)
                "NOTICE" -> parseNotice(ircMessage)
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

    private fun parsePrivateMsg(ircMessage: IrcMessage): ChatEvent.Message? {
        val timestamp = ircMessage.tags.parseTimestamp() ?: clock.now()
        return when (ircMessage.tags.messageId) {
            "highlighted-message" -> {
                ChatEvent.Message.HighlightedMessage(
                    timestamp = timestamp,
                    userMessage = parseMessage(ircMessage) ?: return null,
                )
            }

            "gigantified-emote-message" -> {
                ChatEvent.Message.GigantifiedEmoteMessage(
                    timestamp = timestamp,
                    userMessage = parseMessage(ircMessage) ?: return null,
                )
            }

            else -> {
                parseMessage(ircMessage)
            }
        }
    }

    private fun parseUserNotice(ircMessage: IrcMessage): ChatEvent.Message? {
        val timestamp = ircMessage.tags.parseTimestamp() ?: clock.now()
        return when (ircMessage.tags.messageId) {
            "raid" -> {
                ChatEvent.Message.IncomingRaid(
                    timestamp = timestamp,
                    userDisplayName = ircMessage.tags.displayName ?: return null,
                    raidersCount = ircMessage.tags.raidersCount ?: return null,
                )
            }

            "unraid" -> {
                ChatEvent.Message.CancelledRaid(
                    timestamp = timestamp,
                    userDisplayName = ircMessage.tags.displayName ?: return null,
                )
            }

            "announcement" -> {
                ChatEvent.Message.Announcement(
                    timestamp = timestamp,
                    userMessage = parseMessage(ircMessage) ?: return null,
                )
            }

            "sub", "resub" -> {
                ChatEvent.Message.Subscription(
                    timestamp = timestamp,
                    userDisplayName = ircMessage.tags.displayName ?: return null,
                    subscriptionPlan = ircMessage.tags.subscriptionPlan ?: return null,
                    months = ircMessage.tags.multiMonthDuration ?: 1,
                    streakMonths = ircMessage.tags.streakMonths ?: 0,
                    cumulativeMonths = ircMessage.tags.cumulativeMonths ?: 0,
                    userMessage = parseMessage(ircMessage),
                )
            }

            "giftpaidupgrade", "primepaidupgrade" -> {
                ChatEvent.Message.SubscriptionConversion(
                    timestamp = timestamp,
                    userDisplayName = ircMessage.tags.displayName ?: return null,
                    subscriptionPlan = ircMessage.tags.subscriptionPlan ?: return null,
                    userMessage = parseMessage(ircMessage),
                )
            }

            "submysterygift" -> {
                ChatEvent.Message.MassSubscriptionGift(
                    timestamp = timestamp,
                    userDisplayName = ircMessage.tags.displayName ?: return null,
                    subscriptionPlan = ircMessage.tags.subscriptionPlan ?: return null,
                    giftCount = ircMessage.tags.massGiftCount ?: return null,
                    totalChannelGiftCount = ircMessage.tags.totalChannelGiftCount ?: return null,
                )
            }

            "subgift" -> {
                ChatEvent.Message.SubscriptionGift(
                    timestamp = timestamp,
                    userDisplayName = ircMessage.tags.displayName ?: return null,
                    subscriptionPlan = ircMessage.tags.subscriptionPlan ?: return null,
                    months = ircMessage.tags.giftMonths ?: 1,
                    cumulativeMonths = ircMessage.tags.giftCumulativeMonths ?: 0,
                    recipientDisplayName = ircMessage.tags.recipientDisplayName ?: return null,
                )
            }

            "communitypayforward" -> {
                ChatEvent.Message.GiftPayForward(
                    timestamp = timestamp,
                    userDisplayName = ircMessage.tags.displayName ?: return null,
                    priorGifterDisplayName =
                        ircMessage.tags.priorGifterDisplayName
                            ?.takeUnless { ircMessage.tags.priorGifterAnonymous },
                )
            }

            "modiversary" -> {
                ChatEvent.Message.ModeratorAnniversary(
                    timestamp = timestamp,
                    userDisplayName = ircMessage.tags.displayName ?: return null,
                    months = ircMessage.tags.moderatorMonths ?: return null,
                )
            }

            "viewermilestone" -> {
                when (ircMessage.tags.milestoneCategory) {
                    "watch-streak" -> {
                        ChatEvent.Message.WatchStreak(
                            timestamp = timestamp,
                            userDisplayName = ircMessage.tags.displayName ?: return null,
                            streakLength = ircMessage.tags.milestoneValue ?: return null,
                            userMessage = parseMessage(ircMessage),
                        )
                    }

                    else -> {
                        ChatEvent.Message.UserNotice(
                            timestamp = timestamp,
                            msgId = ircMessage.tags.messageId,
                            systemMsg = ircMessage.tags.systemMsg ?: return null,
                            userMessage = parseMessage(ircMessage),
                        )
                    }
                }
            }

            else -> {
                ChatEvent.Message.UserNotice(
                    timestamp = timestamp,
                    msgId = ircMessage.tags.messageId,
                    systemMsg = ircMessage.tags.systemMsg ?: return null,
                    userMessage = parseMessage(ircMessage),
                )
            }
        }
    }

    private fun parseMessage(ircMessage: IrcMessage): ChatEvent.Message.ChatMessage? {
        val privateMessage =
            PrivMsgMessage.Message.Parser.parse(ircMessage)
                ?: return null

        // If the message is an action, it matches this regex, and we need
        // to extract the actual message contained inside
        val actionGroups = actionRegex.find(privateMessage.message)

        val message = actionGroups?.groupValues?.get(1) ?: privateMessage.message

        return ChatEvent.Message.ChatMessage(
            id = ircMessage.tags.id,
            userId = ircMessage.tags.userId ?: return null,
            userLogin = ircMessage.tags.login ?: privateMessage.source.nick,
            userName = ircMessage.tags.displayName ?: privateMessage.source.nick,
            message = message,
            color = ircMessage.tags.color,
            isAction = actionGroups != null,
            embeddedEmotes = ircMessage.tags.parseEmotes(message).orEmpty(),
            badges = ircMessage.tags.parseBadges(),
            sourceBadges = ircMessage.tags.parseSourceBadges(),
            isFirstMessageByUser = ircMessage.tags.firstMsg,
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
            rewardId = ircMessage.tags.customRewardId,
            inReplyTo = ircMessage.tags.parseParentMessage(),
            sourceRoomId = ircMessage.tags.sourceRoomId,
        )
    }

    private fun parseClearMessage(ircMessage: IrcMessage): ChatEvent =
        ChatEvent.Command.ClearMessage(
            targetMessage = ircMessage.parameters.getOrNull(1),
            targetMessageId = ircMessage.tags.targetMessageId,
            targetUserLogin = ircMessage.tags.login,
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
        )

    private fun parseClearChat(ircMessage: IrcMessage): ChatEvent =
        ChatEvent.Command.ClearChat(
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
            targetUserId = ircMessage.tags.targetUserId,
            targetUserLogin = ircMessage.parameters.getOrNull(1),
            duration = ircMessage.tags.banDuration,
        )

    private fun parseNotice(ircMessage: IrcMessage): ChatEvent.Message.Notice? {
        val notice =
            NoticeMessage.Command.Parser.parse(ircMessage)
                ?: return null

        return ChatEvent.Message.Notice(
            message = notice.message,
            messageId = ircMessage.tags.messageId,
            timestamp = ircMessage.tags.parseTimestamp() ?: clock.now(),
        )
    }

    private fun parseRoomState(ircMessage: IrcMessage): ChatEvent.Command.RoomStateDelta =
        ChatEvent.Command.RoomStateDelta(
            isEmoteOnly = ircMessage.tags.isEmoteOnly,
            minFollowDuration = ircMessage.tags.minFollowDuration,
            uniqueMessagesOnly = ircMessage.tags.uniqueMessagesOnly,
            slowModeDuration = ircMessage.tags.slowModeDuration,
            isSubOnly = ircMessage.tags.isSubOnly,
        )

    private fun parseUserState(ircMessage: IrcMessage): ChatEvent.Command.UserState =
        ChatEvent.Command.UserState(
            emoteSets = ircMessage.tags.emoteSets.orEmpty(),
        )

    companion object {
        private val actionRegex = Regex("^\u0001ACTION (.+)\u0001\$")
    }
}
