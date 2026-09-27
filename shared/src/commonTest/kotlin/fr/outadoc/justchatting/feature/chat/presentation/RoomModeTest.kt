package fr.outadoc.justchatting.feature.chat.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class RoomModeTest {
    @Test
    fun `default room has no modes`() {
        assertEquals(emptyList(), RoomState.Default.activeModes)
    }

    @Test
    fun `followers-only mode with no minimum duration`() {
        assertEquals(
            listOf(RoomMode.FollowersOnly(minFollowDuration = Duration.ZERO)),
            RoomState(minFollowDuration = Duration.ZERO).activeModes,
        )
    }

    @Test
    fun `slow mode is off when its delay is zero`() {
        assertEquals(emptyList(), RoomState(slowModeDuration = Duration.ZERO).activeModes)
    }

    @Test
    fun `all modes are listed in display order`() {
        assertEquals(
            listOf(
                RoomMode.EmoteOnly,
                RoomMode.FollowersOnly(minFollowDuration = 10.minutes),
                RoomMode.UniqueMessages,
                RoomMode.Slow(delay = 30.seconds),
                RoomMode.SubscribersOnly,
            ),
            RoomState(
                isEmoteOnly = true,
                minFollowDuration = 10.minutes,
                uniqueMessagesOnly = true,
                slowModeDuration = 30.seconds,
                isSubOnly = true,
            ).activeModes,
        )
    }
}
