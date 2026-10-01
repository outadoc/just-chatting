package fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.chat

import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEmote
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.Gif
import fr.outadoc.justchatting.feature.emotes.data.twitch.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
internal data class EventSubChatMessageBody(
    @SerialName("text")
    val text: String,
    @SerialName("fragments")
    val fragments: List<Fragment> = emptyList(),
) {
    @Serializable
    data class Fragment(
        @SerialName("type")
        val type: String,
        @SerialName("text")
        val text: String,
        @SerialName("emote")
        val emote: Emote? = null,
        @SerialName("gif")
        val gif: Gif? = null,
    ) {
        @Serializable
        data class Emote(
            @SerialName("id")
            val id: String,
        )

        @Serializable
        data class Gif(
            @SerialName("id")
            val id: String,
            @SerialName("url")
            val url: String,
        )
    }
}

@Serializable
internal data class EventSubChatBadge(
    @SerialName("set_id")
    val setId: String,
    @SerialName("id")
    val id: String,
)

internal fun List<EventSubChatBadge>.toBadges(): List<Badge> = map { badge -> Badge(id = badge.setId, version = badge.id) }

/**
 * Builds a chat message out of the fields common to chat messages and chat notifications.
 *
 * @return null if the message has no text, e.g. a notification without a user message.
 */
internal fun EventSubChatMessageBody.toChatMessage(
    timestamp: Instant,
    messageId: String?,
    userId: String,
    userLogin: String,
    userName: String,
    color: String?,
    badges: List<EventSubChatBadge>,
    sourceBadges: List<EventSubChatBadge>?,
    sourceRoomId: String?,
    isFirstMessageByUser: Boolean = false,
    rewardId: String? = null,
    inReplyTo: ChatEvent.Message.ChatMessage.InReplyTo? = null,
): ChatEvent.Message.ChatMessage? {
    if (text.isBlank()) return null

    val actionGroups = actionRegex.find(text)

    return ChatEvent.Message.ChatMessage(
        timestamp = timestamp,
        id = messageId,
        userId = userId,
        userLogin = userLogin,
        userName = userName,
        message = actionGroups?.groupValues?.get(1) ?: text,
        color = color?.takeUnless { it.isEmpty() },
        isAction = actionGroups != null,
        embeddedEmotes =
            fragments
                .mapNotNull { fragment ->
                    fragment.emote?.let { emote ->
                        ChatEmote(id = emote.id, name = fragment.text).map()
                    }
                }.distinctBy { emote -> emote.name },
        embeddedGifs =
            fragments.mapNotNull { fragment ->
                fragment.gif?.let { gif ->
                    Gif(id = gif.id, url = gif.url, description = fragment.text)
                }
            },
        badges = badges.toBadges(),
        sourceBadges = sourceBadges?.toBadges(),
        isFirstMessageByUser = isFirstMessageByUser,
        rewardId = rewardId?.takeUnless { it.isEmpty() },
        inReplyTo = inReplyTo,
        sourceRoomId = sourceRoomId?.takeUnless { it.isEmpty() },
    )
}

private val actionRegex = Regex("^\u0001ACTION (.+)\u0001$")
