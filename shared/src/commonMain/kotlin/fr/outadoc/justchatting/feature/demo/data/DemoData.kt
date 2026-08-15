package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.DaySchedule
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.utils.datetime.JCLocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Single source of truth for every fixture used by demo mode.
 *
 * Keep fixtures behind named properties/functions so adding demo data later is a one-line edit.
 */
internal object DemoData {
    const val CURRENT_USER_ID: String = "demo-self"
    const val CURRENT_USER_LOGIN: String = "demo_user"
    const val CURRENT_USER_TOKEN: String = "demo-token"

    private val now: Instant get() = Clock.System.now()

    /**
     * Never called from a top-level property initializer that could run before Compose Resources
     * (and, on Android, `androidContext`) are ready — only from inside [user], [globalBadges],
     * [channelBadges], [cheerEmotes] and [setEmotes], which are themselves only first touched once
     * demo mode is actually entered at runtime.
     */
    private fun drawableUri(name: String): String = Res.getUri("drawable/$name.png")

    private fun user(
        id: String,
        login: String,
        displayName: String,
        avatar: String,
        description: String = "",
    ) = User(
        id = id,
        login = login,
        displayName = displayName,
        description = description,
        profileImageUrl = drawableUri(avatar),
        createdAt = now - 365.days,
        usedAt = null,
    )

    val currentUser: User =
        user(
            id = CURRENT_USER_ID,
            login = CURRENT_USER_LOGIN,
            displayName = "Demo User",
            avatar = "demo_avatar_self",
            description = "Just here to try things out.",
        )

    private val ada =
        user(
            id = "demo-ada",
            login = "ada_codes",
            displayName = "ada_codes",
            avatar = "demo_avatar_ada",
            description = "Streaming some Kotlin Multiplatform tonight!",
        )

    private val grace =
        user(
            id = "demo-grace",
            login = "grace_plays",
            displayName = "grace_plays",
            avatar = "demo_avatar_grace",
            description = "Retro games every week.",
        )

    private val alan =
        user(
            id = "demo-alan",
            login = "alan_music",
            displayName = "alan_music",
            avatar = "demo_avatar_alan",
            description = "Lo-fi beats and chill vibes.",
        )

    val channels: List<User> = listOf(ada, grace, alan)

    val allUsers: List<User> = channels + currentUser

    val follows: List<ChannelFollow> =
        channels.map { user ->
            ChannelFollow(user = user, followedAt = now - 30.days)
        }

    private val category = StreamCategory(id = "demo-category", name = "Software and Game Development")

    val liveStreams: List<UserStream> =
        listOf(
            UserStream(
                user = ada,
                stream =
                    Stream(
                        id = "demo-stream-ada",
                        userId = ada.id,
                        category = category,
                        title = "Building a demo mode, of all things",
                        viewerCount = 42,
                        startedAt = now - 90.minutes,
                    ),
            ),
        )

    val futureSchedule: List<DaySchedule> =
        listOf(
            DaySchedule(
                date = JCLocalDate((now + 1.days).toLocalDateTime(TimeZone.currentSystemDefault()).date),
                schedule =
                    listOf(
                        ChannelScheduleSegment(
                            id = "demo-segment-grace",
                            user = grace,
                            startTime = now + 1.days,
                            endTime = now + 1.days + 2.hours,
                            title = "Retro game night",
                            category = category,
                        ),
                    ),
            ),
            DaySchedule(
                date = JCLocalDate((now + 2.days).toLocalDateTime(TimeZone.currentSystemDefault()).date),
                schedule =
                    listOf(
                        ChannelScheduleSegment(
                            id = "demo-segment-alan",
                            user = alan,
                            startTime = now + 2.days,
                            endTime = now + 2.days + 3.hours,
                            title = "Chill beats to code to",
                            category = category,
                        ),
                    ),
            ),
        )

    val searchResults: List<ChannelSearchResult> =
        channels.map { user ->
            val liveStream = liveStreams.firstOrNull { it.user.id == user.id }
            ChannelSearchResult(
                title = liveStream?.stream?.title ?: "Offline",
                user = user,
                gameId = category.id,
                gameName = category.name,
                isLive = liveStream != null,
            )
        }

    val globalBadges: List<TwitchBadge> =
        listOf(
            TwitchBadge(setId = "broadcaster", version = "1", urls = EmoteUrls(url = drawableUri("demo_badge_broadcaster"))),
            TwitchBadge(setId = "moderator", version = "1", urls = EmoteUrls(url = drawableUri("demo_badge_moderator"))),
        )

    val channelBadges: List<TwitchBadge> =
        listOf(
            TwitchBadge(
                setId = "subscriber",
                version = "1",
                title = "1 month",
                urls = EmoteUrls(url = drawableUri("demo_badge_subscriber")),
            ),
        )

    val cheerEmotes: List<Emote> =
        listOf(
            Emote(name = "Cheer100", urls = EmoteUrls(url = drawableUri("demo_emote_cheer")), bitsValue = 100),
        )

    val setEmotes: List<Emote> =
        listOf(
            Emote(name = "demoPog", urls = EmoteUrls(url = drawableUri("demo_emote_pog"))),
            Emote(name = "demoKappa", urls = EmoteUrls(url = drawableUri("demo_emote_kappa"))),
        )

    fun findUser(id: String): User? = allUsers.firstOrNull { it.id == id }

    fun syntheticUser(id: String): User =
        user(
            id = id,
            login = "channel_$id",
            displayName = "channel_$id",
            avatar = "demo_avatar_viewer",
        )

    /** One scripted chat feed entry: how to build the event, and how long to wait before the next one. */
    class ChatScriptEntry(
        val delayAfter: Duration,
        val build: (timestamp: Instant, id: String) -> ChatEvent.Message,
    )

    private fun chatMessage(
        userLogin: String,
        userName: String,
        message: String,
    ): (Instant, String) -> ChatEvent.Message.ChatMessage =
        { timestamp, id ->
            ChatEvent.Message.ChatMessage(
                timestamp = timestamp,
                id = id,
                userId = "demo-viewer-$userLogin",
                userLogin = userLogin,
                userName = userName,
                message = message,
                color = null,
                embeddedEmotes = emptyList(),
                badges = emptyList<Badge>(),
                rewardId = null,
                inReplyTo = null,
            )
        }

    /** Burst shown immediately when the chat screen opens, so it isn't empty. */
    val chatOpeningBurst: List<(Instant, String) -> ChatEvent.Message> =
        listOf(
            chatMessage("pixelfan92", "PixelFan92", "hey chat! 👋"),
            chatMessage("retrogamerx", "RetroGamerX", "pog"),
            chatMessage("lofilistener", "LofiListener", "excited for this one"),
        )

    /** Looping scripted feed, exercising the rich message cards. */
    val chatScript: List<ChatScriptEntry> =
        listOf(
            ChatScriptEntry(delayAfter = 6.seconds) { timestamp, id ->
                chatMessage("pixelfan92", "PixelFan92", "this demo is pretty neat")(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 5.seconds) { timestamp, id ->
                ChatEvent.Message.Announcement(
                    timestamp = timestamp,
                    userMessage = chatMessage(currentUser.login, currentUser.displayName, "Don't forget to follow!")(timestamp, id),
                )
            },
            ChatScriptEntry(delayAfter = 7.seconds) { timestamp, id ->
                chatMessage("retrogamerx", "RetroGamerX", "lol")(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 6.seconds) { timestamp, _ ->
                ChatEvent.Message.Subscription(
                    timestamp = timestamp,
                    userDisplayName = "LofiListener",
                    months = 3,
                    streakMonths = 3,
                    cumulativeMonths = 3,
                    subscriptionPlan = "1000",
                    userMessage = null,
                )
            },
            ChatScriptEntry(delayAfter = 8.seconds) { timestamp, id ->
                chatMessage("lofilistener", "LofiListener", "thanks for having me!")(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 6.seconds) { timestamp, _ ->
                ChatEvent.Message.IncomingRaid(
                    timestamp = timestamp,
                    userDisplayName = "AnotherStreamer",
                    raidersCount = 12,
                )
            },
            ChatScriptEntry(delayAfter = 7.seconds) { timestamp, id ->
                ChatEvent.Message.HighlightedMessage(
                    timestamp = timestamp,
                    userMessage = chatMessage("retrogamerx", "RetroGamerX", "welcome raiders!")(timestamp, id),
                )
            },
            ChatScriptEntry(delayAfter = 6.seconds) { timestamp, id ->
                chatMessage("pixelfan92", "PixelFan92", "GG everyone!")(timestamp, id)
            },
        )
}
