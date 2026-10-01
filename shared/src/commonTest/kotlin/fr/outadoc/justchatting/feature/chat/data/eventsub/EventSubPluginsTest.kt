package fr.outadoc.justchatting.feature.chat.data.eventsub

import fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.channelupdate.EventSubChannelUpdatePlugin
import fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.heldmessage.EventSubHeldMessageUpdatePlugin
import fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.raid.EventSubOutgoingRaidPlugin
import fr.outadoc.justchatting.feature.chat.data.eventsub.plugin.streamstatus.EventSubStreamStatusPlugin
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.feature.preferences.domain.model.ApiToken
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DefaultJson
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

internal class EventSubPluginsTest {
    private val now = Instant.fromEpochMilliseconds(1_000)
    private val clock =
        object : Clock {
            override fun now(): Instant = now
        }

    private fun event(json: String): JsonObject = DefaultJson.parseToJsonElement(json).jsonObject

    @Test
    fun `channel update is mapped to broadcast settings update`() {
        val plugin = EventSubChannelUpdatePlugin(DefaultJson, clock)

        val events =
            plugin.parseEvent(
                event(
                    """
                    {
                        "broadcaster_user_id": "1337",
                        "broadcaster_user_login": "cool_user",
                        "broadcaster_user_name": "Cool_User",
                        "title": "Best Stream Ever",
                        "language": "en",
                        "category_id": "12453",
                        "category_name": "Grand Theft Auto",
                        "content_classification_labels": ["MatureGame"]
                    }
                    """,
                ),
            )

        assertEquals(
            listOf(
                ChatEvent.Message.BroadcastSettingsUpdate(
                    timestamp = now,
                    streamTitle = "Best Stream Ever",
                    categoryId = "12453",
                    categoryName = "Grand Theft Auto",
                ),
            ),
            events,
        )
    }

    @Test
    fun `outgoing raid is mapped to raid go`() {
        val plugin = EventSubOutgoingRaidPlugin(DefaultJson, clock)

        assertEquals(
            mapOf("from_broadcaster_user_id" to "1234"),
            plugin.getCondition(channelId = "1234", appUser = appUser),
        )

        val events =
            plugin.parseEvent(
                event(
                    """
                    {
                        "from_broadcaster_user_id": "1234",
                        "from_broadcaster_user_login": "cool_user",
                        "from_broadcaster_user_name": "Cool_User",
                        "to_broadcaster_user_id": "1337",
                        "to_broadcaster_user_login": "cooler_user",
                        "to_broadcaster_user_name": "Cooler_User",
                        "viewers": 9001
                    }
                    """,
                ),
            )

        assertEquals(
            listOf(
                ChatEvent.Message.RaidUpdate(
                    timestamp = now,
                    raid =
                        Raid.Go(
                            targetId = "1337",
                            targetLogin = "cooler_user",
                            targetDisplayName = "Cooler_User",
                            targetProfileImageUrl = null,
                            viewerCount = 9001,
                        ),
                ),
            ),
            events,
        )
    }

    @Test
    fun `stream status plugins subscribe to the right type`() {
        assertEquals("stream.online", EventSubStreamStatusPlugin(DefaultJson, clock, isLive = true).subscriptionType)
        assertEquals("stream.offline", EventSubStreamStatusPlugin(DefaultJson, clock, isLive = false).subscriptionType)
    }

    @Test
    fun `stream offline is mapped to stream status update`() {
        val plugin = EventSubStreamStatusPlugin(DefaultJson, clock, isLive = false)

        val events =
            plugin.parseEvent(
                event(
                    """
                    {
                        "broadcaster_user_id": "1337",
                        "broadcaster_user_login": "cool_user",
                        "broadcaster_user_name": "Cool_User"
                    }
                    """,
                ),
            )

        assertEquals(
            listOf(
                ChatEvent.Message.StreamStatusUpdate(
                    timestamp = now,
                    isLive = false,
                    broadcasterDisplayName = "Cool_User",
                ),
            ),
            events,
        )
    }

    @Test
    fun `held message update is scoped to the current user`() {
        val plugin = EventSubHeldMessageUpdatePlugin(DefaultJson, clock)

        assertEquals(
            mapOf(
                "broadcaster_user_id" to "1234",
                "user_id" to "5678",
            ),
            plugin.getCondition(channelId = "1234", appUser = appUser),
        )
    }

    @Test
    fun `held message statuses are mapped`() {
        val plugin = EventSubHeldMessageUpdatePlugin(DefaultJson, clock)

        val expected =
            mapOf(
                "approved" to ChatEvent.Message.HeldMessageUpdate.Status.Approved,
                "denied" to ChatEvent.Message.HeldMessageUpdate.Status.Denied,
                "invalid" to ChatEvent.Message.HeldMessageUpdate.Status.Expired,
            )

        expected.forEach { (status, expectedStatus) ->
            assertEquals(
                listOf(
                    ChatEvent.Message.HeldMessageUpdate(
                        timestamp = now,
                        status = expectedStatus,
                        messageText = "hey",
                    ),
                ),
                plugin.parseEvent(heldMessageUpdate(status)),
            )
        }
    }

    @Test
    fun `unknown held message status is ignored`() {
        val plugin = EventSubHeldMessageUpdatePlugin(DefaultJson, clock)
        assertTrue(plugin.parseEvent(heldMessageUpdate("something_new")).isEmpty())
    }

    private fun heldMessageUpdate(status: String): JsonObject =
        event(
            """
            {
                "broadcaster_user_id": "1234",
                "broadcaster_user_login": "streamer",
                "broadcaster_user_name": "Streamer",
                "user_id": "5678",
                "user_login": "viewer",
                "user_name": "Viewer",
                "status": "$status",
                "message_id": "abc",
                "message": {
                    "text": "hey",
                    "fragments": [{ "text": "hey" }]
                }
            }
            """,
        )

    private val appUser =
        AppUser.LoggedIn(
            userId = "5678",
            userLogin = "viewer",
            token = ApiToken("token"),
        )
}
