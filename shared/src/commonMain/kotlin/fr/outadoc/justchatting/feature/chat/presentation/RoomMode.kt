package fr.outadoc.justchatting.feature.chat.presentation

import kotlin.time.Duration

/** A restriction on who can chat, or what they can send. */
public sealed interface RoomMode {
    public data object EmoteOnly : RoomMode

    /** Only followers can chat, if they have been following for at least [minFollowDuration]. */
    public data class FollowersOnly(
        val minFollowDuration: Duration,
    ) : RoomMode

    public data object UniqueMessages : RoomMode

    /** Chatters must wait [delay] between two messages. */
    public data class Slow(
        val delay: Duration,
    ) : RoomMode

    public data object SubscribersOnly : RoomMode
}

/** The modes enabled in this chat, in the order they should be displayed. */
public val RoomState.activeModes: List<RoomMode>
    get() =
        buildList {
            if (isEmoteOnly) add(RoomMode.EmoteOnly)
            if (!minFollowDuration.isNegative()) add(RoomMode.FollowersOnly(minFollowDuration))
            if (uniqueMessagesOnly) add(RoomMode.UniqueMessages)
            if (slowModeDuration.isPositive()) add(RoomMode.Slow(slowModeDuration))
            if (isSubOnly) add(RoomMode.SubscribersOnly)
        }
