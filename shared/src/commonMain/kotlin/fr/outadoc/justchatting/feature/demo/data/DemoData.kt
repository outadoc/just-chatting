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

    /** Same caveat as [drawableUri]: only touched lazily, once demo mode is actually entered. */
    private fun fileUri(name: String): String = Res.getUri("files/$name")

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

    private val solanum =
        user(
            id = "demo-solanum",
            login = "solanum",
            displayName = "Solanum",
            avatar = "demo_avatar_solanum",
            description = "The Eye might have called out to any sentient species. " +
                "Or it might not have been calling out at all.",
        )

    private val yarrow =
        user(
            id = "demo-yarrow",
            login = "yarrow",
            displayName = "Yarrow",
            avatar = "demo_avatar_yarrow",
            description = "Perhaps a change of task would help: Spire noticed a comet " +
                "approaching this star system.",
        )

    private val poke =
        user(
            id = "demo-poke",
            login = "poke",
            displayName = "Poke",
            avatar = "demo_avatar_poke",
            description = "All I can give is my best. And as Annona would say, we will find another way.",
        )

    val channels: List<User> = listOf(solanum, yarrow, poke)

    val allUsers: List<User> = channels + currentUser

    val follows: List<ChannelFollow> =
        channels.map { user ->
            ChannelFollow(user = user, followedAt = now - 30.days)
        }

    private val category = StreamCategory(id = "demo-category", name = "Quantum Archaeology")

    val liveStreams: List<UserStream> =
        listOf(
            UserStream(
                user = solanum,
                stream =
                    Stream(
                        id = "demo-stream-solanum",
                        userId = solanum.id,
                        category = category,
                        title = "We must find this Eye of the universe",
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
                            id = "demo-segment-yarrow",
                            user = yarrow,
                            startTime = now + 1.days,
                            endTime = now + 1.days + 2.hours,
                            title = "Tracking the approaching comet",
                            category = category,
                        ),
                    ),
            ),
            DaySchedule(
                date = JCLocalDate((now + 2.days).toLocalDateTime(TimeZone.currentSystemDefault()).date),
                schedule =
                    listOf(
                        ChannelScheduleSegment(
                            id = "demo-segment-poke",
                            user = poke,
                            startTime = now + 2.days,
                            endTime = now + 2.days + 3.hours,
                            title = "Searching for another way",
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

    /** Real 7TV emotes (om, EEK, Pog, o), bundled as static assets. `ratio` mirrors each source image's width/height. */
    private val omEmote = Emote(name = "om", urls = EmoteUrls(url = fileUri("demo_emote_om.gif")), ratio = 192f / 42f)
    private val eekEmote = Emote(name = "EEK", urls = EmoteUrls(url = drawableUri("demo_emote_eek")))
    private val pogEmote = Emote(name = "Pog", urls = EmoteUrls(url = drawableUri("demo_emote_pog")))
    private val oEmote = Emote(name = "o", urls = EmoteUrls(url = drawableUri("demo_emote_o")), ratio = 192f / 64f)

    val setEmotes: List<Emote> = listOf(omEmote, eekEmote, pogEmote, oEmote)

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
        embeddedEmotes: List<Emote> = emptyList(),
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
                embeddedEmotes = embeddedEmotes,
                badges = emptyList<Badge>(),
                rewardId = null,
                inReplyTo = null,
            )
        }

    /** Burst shown immediately when the chat screen opens, so it isn't empty. */
    val chatOpeningBurst: List<(Instant, String) -> ChatEvent.Message> =
        listOf(
            chatMessage("ramie", "Ramie", "Hypothesis confirmed! Hypothesis confirmed! I saw it! Hypothesis confirmed!"),
            chatMessage("conoy", "Conoy", "I believe I have a solution for that problem!"),
            chatMessage("filix", "Filix", "Hypothesis: This rock shard's presence is significant. We should study it!"),
            chatMessage("daz", "Daz", "om", embeddedEmotes = listOf(omEmote)),
            chatMessage("plume", "Plume", "Suppose this moon is too shy to show us its face."),
            chatMessage("ilex", "Ilex", "Pog", embeddedEmotes = listOf(pogEmote)),
        )

    /** Looping scripted feed, exercising the rich message cards. Text is quoted from Nomai scrolls. */
    val chatScript: List<ChatScriptEntry> =
        listOf(
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, id ->
                chatMessage(
                    "mallow",
                    "Mallow",
                    "The thought of concluding our elders' search increases my heart's temperature!",
                )(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 2.seconds) { timestamp, id ->
                chatMessage("privet", "Privet", "Pog", embeddedEmotes = listOf(pogEmote))(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, id ->
                chatMessage(
                    "phlox",
                    "Phlox",
                    "We can model the Timber Hearth tower after a geyser mountain!",
                )(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, id ->
                ChatEvent.Message.Announcement(
                    timestamp = timestamp,
                    userMessage = chatMessage(
                        solanum.login,
                        solanum.displayName,
                        "I'm entirely delighted! It's never too early to appreciate biology!",
                    )(timestamp, id),
                )
            },
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, id ->
                chatMessage(
                    "coleus",
                    "Coleus",
                    "I'm relieved by our clan's decision to use Timber Hearth's ore only for constructing the shell.",
                )(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 2.seconds) { timestamp, id ->
                chatMessage("clary", "Clary", "o", embeddedEmotes = listOf(oEmote))(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, id ->
                chatMessage("lami", "Lami", "Why are we changing it? It's too hard if you can't see anything!")(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, _ ->
                ChatEvent.Message.Subscription(
                    timestamp = timestamp,
                    userDisplayName = "Avens",
                    months = 3,
                    streakMonths = 3,
                    cumulativeMonths = 3,
                    subscriptionPlan = "1000",
                    userMessage = null,
                )
            },
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, id ->
                chatMessage(
                    "avens",
                    "Avens",
                    "Is the safest path the best one? Our goal is worth the risk.",
                )(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 2.seconds) { timestamp, id ->
                chatMessage("spire", "Spire", "Pog", embeddedEmotes = listOf(pogEmote))(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, id ->
                chatMessage(
                    "thatch",
                    "Thatch",
                    "Imagine what rare and profound knowledge it might offer. We must find this Eye of the universe.",
                )(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 2.seconds) { timestamp, id ->
                chatMessage("ramie", "Ramie", "om", embeddedEmotes = listOf(omEmote))(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, id ->
                chatMessage(
                    "idaea",
                    "Idaea",
                    "I almost can't comprehend this is being suggested seriously.",
                )(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, _ ->
                ChatEvent.Message.IncomingRaid(
                    timestamp = timestamp,
                    userDisplayName = "Idaea",
                    raidersCount = 12,
                )
            },
            ChatScriptEntry(delayAfter = 2.seconds) { timestamp, id ->
                chatMessage("annona", "Annona", "EEK", embeddedEmotes = listOf(eekEmote))(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, id ->
                ChatEvent.Message.HighlightedMessage(
                    timestamp = timestamp,
                    userMessage = chatMessage(
                        "cassava",
                        "Cassava",
                        "I enjoy precision as much as the next Nomai, provided the next Nomai is not @Poke.",
                    )(timestamp, id),
                )
            },
            ChatScriptEntry(delayAfter = 2.seconds) { timestamp, id ->
                chatMessage("root", "Root", "o", embeddedEmotes = listOf(oEmote))(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 3.seconds) { timestamp, id ->
                chatMessage("pye", "Pye", "This is beyond extraordinary! This changes everything!")(timestamp, id)
            },
            ChatScriptEntry(delayAfter = 2.seconds) { timestamp, id ->
                chatMessage("melorae", "Melorae", "Pog Pog", embeddedEmotes = listOf(pogEmote))(timestamp, id)
            },
        )
}
