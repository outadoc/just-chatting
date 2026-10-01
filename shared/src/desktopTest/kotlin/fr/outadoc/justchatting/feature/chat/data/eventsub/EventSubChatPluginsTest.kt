package fr.outadoc.justchatting.feature.chat.data.eventsub

import fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.chat.EventSubChatClearPlugin
import fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.chat.EventSubChatMessagePlugin
import fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.chat.EventSubChatNotificationPlugin
import fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.chat.EventSubChatSettingsPlugin
import fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.chat.EventSubClearUserMessagesPlugin
import fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.chat.EventSubMessageDeletePlugin
import fr.outadoc.justchatting.feature.chat.data.irc.TwitchIrcCommandParser
import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesApi
import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesRepository
import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesResponse
import fr.outadoc.justchatting.feature.chat.domain.eventsub.EventSubPlugin
import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.shared.data.TwitchClient
import fr.outadoc.justchatting.utils.core.DefaultJson
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal class EventSubChatPluginsTest {
    private val now = Instant.fromEpochMilliseconds(1_000)

    // Only used for fetching initial events, which these tests don't cover
    private val twitchClient = TwitchClient(HttpClient())

    private val chatMessagePlugin =
        EventSubChatMessagePlugin(
            json = DefaultJson,
            clock = Clock.System,
            twitchClient = twitchClient,
            recentMessagesRepository =
                RecentMessagesRepository(
                    recentMessagesApi =
                        object : RecentMessagesApi {
                            override suspend fun getRecentMessages(
                                channelLogin: String,
                                limit: Int,
                            ): Result<RecentMessagesResponse> = error("Not used in tests")
                        },
                    parser = TwitchIrcCommandParser(Clock.System),
                    dispatchersProvider =
                        object : DispatchersProvider {
                            override val io: CoroutineDispatcher = Dispatchers.Unconfined
                            override val default: CoroutineDispatcher = Dispatchers.Unconfined
                        },
                ),
            preferenceRepository =
                object : PreferenceRepository {
                    override val currentPreferences: Flow<AppPreferences> = flowOf(AppPreferences())

                    override suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences) = Unit
                },
        )

    private val notificationPlugin = EventSubChatNotificationPlugin(DefaultJson)

    private fun EventSubPlugin.parseEvent(json: String): List<ChatEvent> =
        parseEvent(
            event = DefaultJson.parseToJsonElement(json).jsonObject,
            timestamp = now,
        )

    private fun chatMessage(
        messageType: String = "text",
        text: String = "Hi chat",
        fragments: String = """[{ "type": "text", "text": "Hi chat" }]""",
        extra: String = "",
    ): String =
        """
        {
            "broadcaster_user_id": "1971641",
            "broadcaster_user_login": "streamer",
            "broadcaster_user_name": "streamer",
            "chatter_user_id": "4145994",
            "chatter_user_login": "viewer32",
            "chatter_user_name": "viewer32",
            "message_id": "cc106a89-1814-919d-454c-f4f2f970aae7",
            "message": {
                "text": "$text",
                "fragments": $fragments
            },
            "color": "#00FF7F",
            "badges": [
                { "set_id": "moderator", "id": "1", "info": "" },
                { "set_id": "subscriber", "id": "12", "info": "16" }
            ],
            "message_type": "$messageType",
            "cheer": null,
            "reply": null,
            "channel_points_custom_reward_id": null
            $extra
        }
        """

    @Test
    fun `chat message is parsed`() {
        val events = chatMessagePlugin.parseEvent(chatMessage())

        assertEquals(
            listOf(
                ChatEvent.Message.ChatMessage(
                    timestamp = now,
                    id = "cc106a89-1814-919d-454c-f4f2f970aae7",
                    userId = "4145994",
                    userLogin = "viewer32",
                    userName = "viewer32",
                    message = "Hi chat",
                    color = "#00FF7F",
                    embeddedEmotes = emptyList(),
                    badges = listOf(Badge("moderator", "1"), Badge("subscriber", "12")),
                    rewardId = null,
                    inReplyTo = null,
                ),
            ),
            events,
        )
    }

    @Test
    fun `chat message emotes are extracted from fragments`() {
        val events =
            chatMessagePlugin.parseEvent(
                chatMessage(
                    text = "Hi Kappa",
                    fragments =
                        """
                        [
                            { "type": "text", "text": "Hi " },
                            {
                                "type": "emote",
                                "text": "Kappa",
                                "emote": { "id": "25", "emote_set_id": "0", "owner_id": "0", "format": ["static"] }
                            }
                        ]
                        """,
                ),
            )

        val message = assertIs<ChatEvent.Message.ChatMessage>(events.single())
        assertEquals(listOf("Kappa"), message.embeddedEmotes.map { emote -> emote.name })
    }

    @Test
    fun `chat message reply is parsed`() {
        val events =
            chatMessagePlugin.parseEvent(
                chatMessage(
                    extra =
                        """
                        , "reply": {
                            "parent_message_id": "parent-id",
                            "parent_message_body": "original message",
                            "parent_user_id": "123",
                            "parent_user_name": "Someone",
                            "parent_user_login": "someone",
                            "thread_message_id": "parent-id",
                            "thread_user_id": "123",
                            "thread_user_name": "Someone",
                            "thread_user_login": "someone"
                        }
                        """,
                ).replace("\"reply\": null,", ""),
            )

        val message = assertIs<ChatEvent.Message.ChatMessage>(events.single())
        assertEquals(
            ChatEvent.Message.ChatMessage.InReplyTo(
                id = "parent-id",
                message = "original message",
                userId = "123",
                userLogin = "someone",
                userDisplayName = "Someone",
            ),
            message.inReplyTo,
        )
    }

    @Test
    fun `highlighted chat message is wrapped`() {
        val events = chatMessagePlugin.parseEvent(chatMessage(messageType = "channel_points_highlighted"))
        assertIs<ChatEvent.Message.HighlightedMessage>(events.single())
    }

    @Test
    fun `shared chat message keeps its source channel`() {
        val events =
            chatMessagePlugin.parseEvent(
                chatMessage(
                    extra =
                        """
                        , "source_broadcaster_user_id": "112233",
                        "source_message_id": "source-id",
                        "source_badges": [{ "set_id": "vip", "id": "1", "info": "" }]
                        """,
                ),
            )

        val message = assertIs<ChatEvent.Message.ChatMessage>(events.single())
        assertEquals("112233", message.sourceRoomId)
        assertEquals(listOf(Badge("vip", "1")), message.sourceBadges)
    }

    private fun notification(
        noticeType: String,
        details: String,
        text: String = "",
        anonymous: Boolean = false,
    ): String =
        """
        {
            "broadcaster_user_id": "1971641",
            "broadcaster_user_login": "streamer",
            "broadcaster_user_name": "streamer",
            "chatter_user_id": ${if (anonymous) "null" else "\"49912639\""},
            "chatter_user_login": ${if (anonymous) "null" else "\"viewer23\""},
            "chatter_user_name": ${if (anonymous) "null" else "\"viewer23\""},
            "chatter_is_anonymous": $anonymous,
            "color": "",
            "badges": [],
            "system_message": "System message",
            "message_id": "d62235c8-47ff-a4f4--84e8-5a29a65a9c03",
            "message": { "text": "$text", "fragments": [] },
            "notice_type": "$noticeType",
            "$noticeType": $details
        }
        """

    @Test
    fun `resub notification is parsed`() {
        val events =
            notificationPlugin.parseEvent(
                notification(
                    noticeType = "resub",
                    details =
                        """
                        {
                            "cumulative_months": 10,
                            "duration_months": 1,
                            "streak_months": 3,
                            "sub_tier": "2000",
                            "is_prime": false,
                            "is_gift": false
                        }
                        """,
                    text = "Love this stream",
                ),
            )

        val sub = assertIs<ChatEvent.Message.Subscription>(events.single())
        assertEquals("viewer23", sub.userDisplayName)
        assertEquals("2000", sub.subscriptionPlan)
        assertEquals(10, sub.cumulativeMonths)
        assertEquals(3, sub.streakMonths)
        assertEquals("Love this stream", sub.userMessage?.message)
    }

    @Test
    fun `prime sub notification is parsed`() {
        val events =
            notificationPlugin.parseEvent(
                notification(
                    noticeType = "sub",
                    details = """{ "sub_tier": "1000", "is_prime": true, "duration_months": 1 }""",
                ),
            )

        val sub = assertIs<ChatEvent.Message.Subscription>(events.single())
        assertEquals("Prime", sub.subscriptionPlan)
        assertNull(sub.userMessage)
    }

    @Test
    fun `anonymous community gift notification is parsed`() {
        val events =
            notificationPlugin.parseEvent(
                notification(
                    noticeType = "community_sub_gift",
                    details = """{ "id": "1", "total": 5, "sub_tier": "1000", "cumulative_total": null }""",
                    anonymous = true,
                ),
            )

        val gift = assertIs<ChatEvent.Message.MassSubscriptionGift>(events.single())
        assertEquals("AnAnonymousGifter", gift.userDisplayName)
        assertEquals(5, gift.giftCount)
        assertNull(gift.totalChannelGiftCount)
    }

    @Test
    fun `shared chat raid notification is parsed`() {
        val events =
            notificationPlugin.parseEvent(
                notification(
                    noticeType = "shared_chat_raid",
                    details =
                        """
                        {
                            "user_id": "123",
                            "user_name": "Raider",
                            "user_login": "raider",
                            "viewer_count": 42,
                            "profile_image_url": "https://example.com/raider.png"
                        }
                        """,
                ),
            )

        assertEquals(
            listOf(
                ChatEvent.Message.IncomingRaid(
                    timestamp = now,
                    userDisplayName = "Raider",
                    raidersCount = 42,
                ),
            ),
            events,
        )
    }

    @Test
    fun `unknown notification falls back to the system message`() {
        val events =
            notificationPlugin.parseEvent(
                notification(
                    noticeType = "bits_badge_tier",
                    details = """{ "tier": 1000 }""",
                ),
            )

        val notice = assertIs<ChatEvent.Message.UserNotice>(events.single())
        assertEquals("System message", notice.systemMsg)
        assertEquals("bits_badge_tier", notice.msgId)
    }

    @Test
    fun `message delete is mapped to clear message`() {
        val events =
            EventSubMessageDeletePlugin(DefaultJson).parseEvent(
                """
                {
                    "broadcaster_user_id": "1971641",
                    "broadcaster_user_login": "streamer",
                    "broadcaster_user_name": "streamer",
                    "target_user_id": "7734",
                    "target_user_login": "uncool_viewer",
                    "target_user_name": "uncool_viewer",
                    "message_id": "ab24e0b0-2260-4bac-94e4-05eedd4ecd0e"
                }
                """,
            )

        assertEquals(
            listOf(
                ChatEvent.Command.ClearMessage(
                    timestamp = now,
                    targetMessage = null,
                    targetMessageId = "ab24e0b0-2260-4bac-94e4-05eedd4ecd0e",
                    targetUserLogin = "uncool_viewer",
                ),
            ),
            events,
        )
    }

    @Test
    fun `clear user messages is mapped`() {
        val events =
            EventSubClearUserMessagesPlugin(DefaultJson).parseEvent(
                """
                {
                    "broadcaster_user_id": "1971641",
                    "broadcaster_user_login": "streamer",
                    "broadcaster_user_name": "streamer",
                    "target_user_id": "7734",
                    "target_user_login": "uncool_viewer",
                    "target_user_name": "uncool_viewer"
                }
                """,
            )

        assertEquals(
            listOf(
                ChatEvent.Command.ClearUserMessages(
                    timestamp = now,
                    targetUserId = "7734",
                    targetUserLogin = "uncool_viewer",
                ),
            ),
            events,
        )
    }

    @Test
    fun `chat clear clears everything`() {
        val events = EventSubChatClearPlugin().parseEvent("""{ "broadcaster_user_id": "1971641" }""")

        assertEquals(
            listOf(
                ChatEvent.Command.ClearChat(
                    timestamp = now,
                    targetUserId = null,
                    targetUserLogin = null,
                    duration = null,
                ),
            ),
            events,
        )
    }

    @Test
    fun `chat settings update is mapped to room state`() {
        val events =
            EventSubChatSettingsPlugin(DefaultJson, twitchClient).parseEvent(
                """
                {
                    "broadcaster_user_id": "1337",
                    "broadcaster_user_login": "cool_user",
                    "broadcaster_user_name": "Cool_User",
                    "emote_mode": true,
                    "follower_mode": false,
                    "follower_mode_duration_minutes": null,
                    "slow_mode": true,
                    "slow_mode_wait_time_seconds": 10,
                    "subscriber_mode": false,
                    "unique_chat_mode": false
                }
                """,
            )

        assertEquals(
            listOf(
                ChatEvent.Command.RoomStateDelta(
                    isEmoteOnly = true,
                    minFollowDuration = (-1).minutes,
                    uniqueMessagesOnly = false,
                    slowModeDuration = 10.seconds,
                    isSubOnly = false,
                ),
            ),
            events,
        )
    }

    @Test
    fun `follower mode duration is mapped`() {
        val events =
            EventSubChatSettingsPlugin(DefaultJson, twitchClient).parseEvent(
                """
                {
                    "emote_mode": false,
                    "follower_mode": true,
                    "follower_mode_duration_minutes": 0,
                    "slow_mode": false,
                    "slow_mode_wait_time_seconds": null,
                    "subscriber_mode": true,
                    "unique_chat_mode": true
                }
                """,
            )

        val roomState = assertIs<ChatEvent.Command.RoomStateDelta>(events.single())
        assertEquals(Duration.ZERO, roomState.minFollowDuration)
        assertEquals(Duration.ZERO, roomState.slowModeDuration)
    }
}
