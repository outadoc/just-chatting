package fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.chat

import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesRepository
import fr.outadoc.justchatting.feature.chat.domain.eventsub.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.data.TwitchClient
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Chat messages, including the user's own. Requires the `user:read:chat` scope.
 *
 * Once subscribed, also takes care of what joining a channel over IRC used to provide:
 * the user's emote sets, and backfilling recent messages.
 */
internal class EventSubChatMessagePlugin(
    private val json: Json,
    private val clock: Clock,
    private val twitchClient: TwitchClient,
    private val recentMessagesRepository: RecentMessagesRepository,
    private val preferenceRepository: PreferenceRepository,
) : EventSubPlugin {
    private companion object {
        const val GLOBAL_EMOTE_SET_ID = "0"
    }

    override val subscriptionType = "channel.chat.message"
    override val subscriptionVersion = "1"

    override fun getCondition(
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): Map<String, String> =
        mapOf(
            "broadcaster_user_id" to channelId,
            "user_id" to appUser.userId,
        )

    override fun parseEvent(
        event: JsonObject,
        timestamp: Instant,
    ): List<ChatEvent> {
        val message = json.decodeFromJsonElement(Event.serializer(), event)

        val chatMessage =
            message.message.toChatMessage(
                timestamp = timestamp,
                messageId = message.messageId,
                userId = message.chatterUserId,
                userLogin = message.chatterUserLogin,
                userName = message.chatterUserName,
                color = message.color,
                badges = message.badges,
                sourceBadges = message.sourceBadges,
                sourceRoomId = message.sourceBroadcasterUserId,
                isFirstMessageByUser = message.messageType == "user_intro",
                rewardId = message.channelPointsCustomRewardId,
                inReplyTo =
                    message.reply?.let { reply ->
                        ChatEvent.Message.ChatMessage.InReplyTo(
                            id = reply.parentMessageId,
                            message = reply.parentMessageBody,
                            userId = reply.parentUserId,
                            userLogin = reply.parentUserLogin,
                            userDisplayName = reply.parentUserName,
                        )
                    },
            ) ?: return emptyList()

        return listOf(
            when (message.messageType) {
                "channel_points_highlighted" -> {
                    ChatEvent.Message.HighlightedMessage(
                        timestamp = timestamp,
                        userMessage = chatMessage,
                    )
                }

                "power_ups_gigantified_emote" -> {
                    ChatEvent.Message.GigantifiedEmoteMessage(
                        timestamp = timestamp,
                        userMessage = chatMessage,
                    )
                }

                else -> {
                    chatMessage
                }
            },
        )
    }

    override suspend fun getInitialEvents(
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): List<ChatEvent> =
        coroutineScope {
            val emoteSets = async { getUserEmoteSets(channelId, appUser) }
            val recentMessages = async { getRecentMessages(channelLogin) }

            buildList {
                add(
                    ChatEvent.Message.Join(
                        timestamp = clock.now(),
                        channelLogin = channelLogin,
                    ),
                )
                add(ChatEvent.Command.UserState(emoteSets = emoteSets.await()))
                addAll(recentMessages.await())
            }
        }

    /**
     * Lists the emote sets the user can use in this channel, including follower emotes.
     */
    private suspend fun getUserEmoteSets(
        channelId: String,
        appUser: AppUser.LoggedIn,
    ): List<String> {
        // Helix doesn't list the global emote set, which IRC used to include
        val emoteSets = mutableSetOf(GLOBAL_EMOTE_SET_ID)
        var cursor: String? = null

        do {
            val result =
                twitchClient.getUserEmotes(
                    userId = appUser.userId,
                    channelUserId = channelId,
                    after = cursor,
                )

            result.onFailure { e ->
                logError<EventSubChatMessagePlugin>(e) { "Failed to load user emotes" }
            }

            val response = result.getOrNull() ?: break
            response.data.mapTo(emoteSets) { emote -> emote.emoteSetId }
            cursor = response.pagination?.cursor?.takeUnless { it.isEmpty() }
        } while (cursor != null)

        return emoteSets.toList()
    }

    private suspend fun getRecentMessages(channelLogin: String): List<ChatEvent> {
        val prefs = preferenceRepository.currentPreferences.first()
        if (!prefs.enableRecentMessages) return emptyList()

        return recentMessagesRepository
            .loadRecentMessages(
                channelLogin = channelLogin,
                limit = AppPreferences.Defaults.RecentChatLimit,
            ).map { messages -> messages.filterIsInstance<ChatEvent.Message>() }
            .getOrElse { e ->
                logError<EventSubChatMessagePlugin>(e) { "Failed to load recent messages for channel $channelLogin" }
                emptyList()
            }
    }

    @Serializable
    private data class Event(
        @SerialName("chatter_user_id")
        val chatterUserId: String,
        @SerialName("chatter_user_login")
        val chatterUserLogin: String,
        @SerialName("chatter_user_name")
        val chatterUserName: String,
        @SerialName("message_id")
        val messageId: String,
        @SerialName("message")
        val message: EventSubChatMessageBody,
        @SerialName("message_type")
        val messageType: String,
        @SerialName("badges")
        val badges: List<EventSubChatBadge> = emptyList(),
        @SerialName("color")
        val color: String? = null,
        @SerialName("reply")
        val reply: Reply? = null,
        @SerialName("channel_points_custom_reward_id")
        val channelPointsCustomRewardId: String? = null,
        @SerialName("source_broadcaster_user_id")
        val sourceBroadcasterUserId: String? = null,
        @SerialName("source_badges")
        val sourceBadges: List<EventSubChatBadge>? = null,
    ) {
        @Serializable
        data class Reply(
            @SerialName("parent_message_id")
            val parentMessageId: String,
            @SerialName("parent_message_body")
            val parentMessageBody: String,
            @SerialName("parent_user_id")
            val parentUserId: String,
            @SerialName("parent_user_login")
            val parentUserLogin: String,
            @SerialName("parent_user_name")
            val parentUserName: String,
        )
    }
}
