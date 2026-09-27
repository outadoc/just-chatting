package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote

/**
 * Whether this message should be hidden, because a moderator deleted it or timed out or banned
 * its author.
 */
public fun ChatListItem.Message.isRedacted(removedContent: List<ChatListItem.RemoveContent>): Boolean =
    removedContent
        .filter { rule -> rule.upUntil > timestamp }
        .filter { rule -> rule.matchingMessageId == null || rule.matchingMessageId == body?.messageId }
        .any { rule -> rule.matchingUserId == null || rule.matchingUserId == body?.chatter?.id }

/**
 * The emotes used in this message, each listed once.
 *
 * [ChatListItem.Message.Body.embeddedEmotes] only covers first-party Twitch emotes, resolved from
 * the IRC `emotes` tag. Third-party emotes (BTTV/FFZ/7TV) are just plain words that happen to
 * match a name in the channel's [emotes], so they need to be matched the same way.
 */
public fun ChatListItem.Message.Body.usedEmotes(emotes: Map<String, Emote>): List<Emote> {
    val words = message?.split(' ').orEmpty()
    return (embeddedEmotes + words.mapNotNull { word -> emotes[word] })
        .distinctBy { emote -> emote.name }
}

/**
 * The badges to show next to a chatter's name.
 *
 * @property sourceRoomId set when the message was relayed from another channel in a shared chat,
 * in which case [badges] are the ones the chatter has in that channel.
 */
public data class DisplayedBadges(
    val badges: List<Badge>,
    val sourceRoomId: String?,
)

public val ChatListItem.Message.Body.displayedBadges: DisplayedBadges
    get() =
        if (sourceBadges.isNotEmpty()) {
            DisplayedBadges(badges = sourceBadges, sourceRoomId = sourceRoomId)
        } else {
            DisplayedBadges(badges = badges, sourceRoomId = null)
        }
