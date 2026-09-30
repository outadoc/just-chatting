package fr.outadoc.justchatting.landing

import androidx.compose.ui.graphics.Color
import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.domain.model.Icon
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.chat.presentation.AutoCompleteItem
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.preferences.domain.model.ApiToken
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.feature.timeline.presentation.OngoingScheduleSegment
import fr.outadoc.justchatting.feature.timeline.presentation.ScheduleDay
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.chat_source_stv
import fr.outadoc.justchatting.shared.internal.chat_source_twitch
import fr.outadoc.justchatting.shared.internal.chat_sub_header_withDurationAndStreak
import fr.outadoc.justchatting.shared.internal.chat_sub_tier1
import fr.outadoc.justchatting.shared.internal.months
import fr.outadoc.justchatting.utils.datetime.JCLocalDate
import fr.outadoc.justchatting.utils.resources.desc
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * The content shown in the landing page screenshots. It mirrors the app's demo mode (its
 * channels, chatters and quotes from Nomai scrolls, and its bundled images), but at fixed
 * times, so that the screenshots only change when the UI does.
 *
 * Image URLs are paths to the app's bundled resources, which [landingImagePreviewHandler]
 * loads, since screenshot tests can't reach the network.
 */
internal object LandingFixtures {
    val now: Instant = Instant.parse("2026-09-30T12:05:00Z")

    val clock: Clock =
        object : Clock {
            override fun now(): Instant = now
        }

    private fun drawable(name: String) = "drawable/$name.png"

    private fun user(
        id: String,
        displayName: String,
        avatar: String,
    ) = User(
        id = id,
        login = displayName.lowercase(),
        displayName = displayName,
        description = "",
        profileImageUrl = drawable(avatar),
        createdAt = now - 365.days,
        usedAt = null,
    )

    // Each channel's seed color is the color of its avatar, which is what the app extracts
    // its palette from. The extraction runs asynchronously, so it can't happen in a screenshot.
    val yarrow = user(id = "demo-yarrow", displayName = "Yarrow", avatar = "demo_avatar_yarrow")
    val yarrowSeed = Color(0xFF2EA059)

    val solanum = user(id = "demo-solanum", displayName = "Solanum", avatar = "demo_avatar_solanum")
    val solanumSeed = Color(0xFFE06437)

    val poke = user(id = "demo-poke", displayName = "Poke", avatar = "demo_avatar_poke")
    val pokeSeed = Color(0xFF3B82C4)

    val appUserAvatar: String = drawable("demo_avatar_self")

    val appUser: AppUser.LoggedIn =
        AppUser.LoggedIn(
            userId = "demo-self",
            userLogin = "demo_user",
            token = ApiToken("demo-token"),
        )

    private val category = StreamCategory(id = "demo-category", name = "Quantum Archaeology")
    private val astronomy = StreamCategory(id = "demo-category-astronomy", name = "Astronomy")
    private val engineering = StreamCategory(id = "demo-category-engineering", name = "Engineering")
    private val justChatting = StreamCategory(id = "demo-category-just-chatting", name = "Just Chatting")

    private fun stream(
        user: User,
        title: String,
        viewerCount: Long,
        startedAt: Instant,
        category: StreamCategory = this.category,
    ) = UserStream(
        user = user,
        stream =
            Stream(
                id = "demo-stream-${user.login}",
                userId = user.id,
                category = category,
                title = title,
                viewerCount = viewerCount,
                startedAt = startedAt,
            ),
    )

    val yarrowStream = stream(yarrow, title = "Scieeeeence!", viewerCount = 17, startedAt = now - 40.minutes)
    val solanumStream = stream(solanum, title = "Exploring the universe", viewerCount = 42, startedAt = now - 90.minutes)
    val pokeStream = stream(poke, title = "Working on my new project", viewerCount = 8, startedAt = now - 20.minutes)

    // More live channels, like the demo mode's, so that the list fills the screen.
    private fun channel(name: String) = user(id = "demo-${name.lowercase()}", displayName = name, avatar = "demo_avatar_${name.lowercase()}")

    val liveStreams: ImmutableList<UserStream> =
        persistentListOf(
            yarrowStream,
            solanumStream,
            pokeStream,
            stream(channel("Pye"), "Surveying the Hanging City", viewerCount = 31, startedAt = now - 2.hours, category = astronomy),
            stream(channel("Melorae"), "Charting the moon's orbit", viewerCount = 26, startedAt = now - 75.minutes, category = astronomy),
            stream(channel("Spire"), "Watching the comet approach", viewerCount = 23, startedAt = now - 3.hours, category = astronomy),
            stream(channel("Idaea"), "Reading scrolls with chat", viewerCount = 14, startedAt = now - 50.minutes, category = justChatting),
            stream(channel("Cassava"), "Precise measurements only", viewerCount = 12, startedAt = now - 30.minutes, category = engineering),
            stream(channel("Annona"), "Mapping the quantum moon", viewerCount = 9, startedAt = now - 65.minutes),
            stream(channel("Mallow"), "Building a warp core", viewerCount = 6, startedAt = now - 15.minutes, category = engineering),
            stream(channel("Filix"), "Studying rock shards", viewerCount = 4, startedAt = now - 10.minutes),
        )

    // Emotes and badges

    val pog = Emote(name = "Pog", urls = EmoteUrls(url = drawable("demo_emote_pog")))
    val eek = Emote(name = "EEK", urls = EmoteUrls(url = drawable("demo_emote_eek")), ratio = 68f / 64f)
    val o = Emote(name = "o", urls = EmoteUrls(url = drawable("demo_emote_o")), ratio = 192f / 64f)
    val joel = Emote(name = "Joel", urls = EmoteUrls(url = "files/demo_emote_joel.gif"), ratio = 192f / 64f)

    val recentEmotes: List<Emote> = listOf(pog, joel, eek, o)

    private fun twitchEmote(
        name: String,
        file: String,
        ratio: Float = 1f,
    ) = Emote(name = name, urls = EmoteUrls(url = drawable("demo_emote_twitch_$file")), ratio = ratio)

    // The first global Twitch emotes, like in the demo mode: three full rows in the emote picker
    // of the screenshots.
    private val globalEmotes: List<Emote> =
        listOf(
            twitchEmote(";)", "winky", ratio = 72f / 54f),
            twitchEmote(";P", "winky_tongue", ratio = 72f / 54f),
            twitchEmote(":(", "frown", ratio = 72f / 54f),
            twitchEmote(":)", "smile", ratio = 72f / 54f),
            twitchEmote(":/", "slant", ratio = 72f / 54f),
            twitchEmote(":D", "grin", ratio = 72f / 54f),
            twitchEmote(":O", "surprised", ratio = 72f / 54f),
            twitchEmote(":P", "tongue", ratio = 72f / 54f),
            twitchEmote(":z", "zipped", ratio = 72f / 54f),
            twitchEmote("<3", "heart"),
            twitchEmote(">(", "angry", ratio = 72f / 54f),
            twitchEmote("4Head", "4head", ratio = 60f / 90f),
            twitchEmote("AmbessaLove", "ambessalove"),
            twitchEmote("AndTime", "andtime"),
            twitchEmote("ANELE", "anele"),
            twitchEmote("AnotherRecord", "anotherrecord"),
            twitchEmote("ArgieB8", "argieb8"),
            twitchEmote("ArsonNoSexy", "arsonnosexy", ratio = 54f / 81f),
            twitchEmote("AsexualPride", "asexualpride"),
            twitchEmote("AsianGlow", "asianglow", ratio = 72f / 90f),
            twitchEmote("B)", "cool", ratio = 72f / 54f),
        )

    val pickableEmotes: ImmutableList<EmoteSetItem> =
        (
            listOf(
                EmoteSetItem.Header(title = "Yarrow".desc(), source = Res.string.chat_source_stv.desc()),
                EmoteSetItem.Emote(pog),
                EmoteSetItem.Emote(joel),
                EmoteSetItem.Emote(eek),
                EmoteSetItem.Emote(o),
                EmoteSetItem.Header(title = "Global".desc(), source = Res.string.chat_source_twitch.desc()),
            ) + globalEmotes.map { emote -> EmoteSetItem.Emote(emote) }
        ).toPersistentList()

    private val channelBadges: PersistentList<TwitchBadge> =
        persistentListOf(
            TwitchBadge(
                setId = "subscriber",
                version = "1",
                title = "1 month",
                urls = EmoteUrls(url = drawable("demo_badge_subscriber")),
            ),
        )

    private val globalBadges: PersistentList<TwitchBadge> =
        persistentListOf(
            TwitchBadge(setId = "broadcaster", version = "1", urls = EmoteUrls(url = drawable("demo_badge_broadcaster"))),
            TwitchBadge(setId = "moderator", version = "1", urls = EmoteUrls(url = drawable("demo_badge_moderator"))),
        )

    // Chat messages, quoted from Nomai scrolls like the demo mode's

    private fun message(
        name: String,
        text: String,
        minutesAgo: Int,
        embeddedEmotes: List<Emote> = emptyList(),
        badges: List<Badge> = emptyList(),
    ) = ChatListItem.Message.Simple(
        body =
            ChatListItem.Message.Body(
                messageId = "demo-${name.lowercase()}-$minutesAgo",
                message = text,
                chatter = Chatter(id = name.lowercase(), login = name.lowercase(), displayName = name),
                embeddedEmotes = embeddedEmotes.toPersistentList(),
                badges = badges.toPersistentList(),
            ),
        timestamp = now - minutesAgo.minutes,
    )

    // Older messages, to fill the chat on every screen size. Oldest last.
    private val history: List<ChatListItem.Message> =
        listOf(
            message("Bur", "I found your note, Melorae; kindly count me among this moon's admirers!", minutesAgo = 3),
            message("Annona", "EEK", minutesAgo = 3, embeddedEmotes = listOf(eek)),
            message(
                "Cassava",
                "I enjoy precision as much as the next Nomai, provided the next Nomai is not @Poke.",
                minutesAgo = 3,
            ),
            message("Idaea", "I almost can't comprehend this is being suggested seriously.", minutesAgo = 4),
            message("Clary", "o", minutesAgo = 4, embeddedEmotes = listOf(o)),
            message(
                "Thatch",
                "Imagine what rare and profound knowledge it might offer. We must find this Eye of the universe.",
                minutesAgo = 4,
                badges = listOf(Badge(id = "moderator", version = "1")),
            ),
            message("Spire", "Pog", minutesAgo = 5, embeddedEmotes = listOf(pog)),
            message(
                "Coleus",
                "I'm relieved by our clan's decision to use Timber Hearth's ore only for constructing the shell.",
                minutesAgo = 5,
            ),
            message("Phlox", "We can model the Timber Hearth tower after a geyser mountain!", minutesAgo = 6),
            message("Privet", "Joel", minutesAgo = 6, embeddedEmotes = listOf(joel)),
            message(
                "Mallow",
                "The thought of concluding our elders' search increases my heart's temperature!",
                minutesAgo = 7,
            ),
        )

    private val conoy = message("Conoy", "I believe I have a solution for that problem!", minutesAgo = 2)
    private val filix =
        message("Filix", "Hypothesis: This rock shard's presence is significant. We should study it!", minutesAgo = 2)
    val ramie = message("Ramie", "Hypothesis confirmed! Hypothesis confirmed! I saw it! Hypothesis confirmed!", minutesAgo = 1)
    val plume = message("Plume", "Suppose this moon is too shy to show us its face.", minutesAgo = 1)
    val avens =
        message(
            "Avens",
            "Is the safest path the best one? Our goal is worth the risk.",
            minutesAgo = 0,
            badges = listOf(Badge(id = "subscriber", version = "1")),
        )
    val lami = message("Lami", "Why are we changing it? It's too hard if you can't see anything!", minutesAgo = 0)
    val pye = message("Pye", "This is beyond extraordinary! This changes everything!", minutesAgo = 0)
    val solanumMessage =
        message(
            "Solanum",
            "I'm entirely delighted! It's never too early to appreciate biology!",
            minutesAgo = 0,
            badges = listOf(Badge(id = "broadcaster", version = "1")),
        )
    val pogMessage = message("Ilex", "Pog", minutesAgo = 0, embeddedEmotes = listOf(pog))

    val avensSubscription =
        ChatListItem.Message.Highlighted(
            timestamp = now,
            metadata =
                ChatListItem.Message.Highlighted.Metadata(
                    title = "Avens".desc(),
                    titleIcon = Icon.Star,
                    subtitle =
                        Res.string.chat_sub_header_withDurationAndStreak.desc(
                            Res.string.chat_sub_tier1.desc(),
                            Res.plurals.months.desc(3, "3"),
                            Res.plurals.months.desc(3, "3"),
                        ),
                ),
            body = null,
        )

    val pronouns =
        persistentMapOf(
            solanumMessage.body.chatter to Pronoun(id = "sheher", nominative = "she", objective = "her", isSingular = true),
            avens.body.chatter to Pronoun(id = "hehim", nominative = "he", objective = "him", isSingular = true),
        )

    // Newest first, as the chat list expects them.
    val chatMessages: PersistentList<ChatListItem.Message> =
        (listOf(pye, lami, pogMessage, avens, avensSubscription, plume, ramie, filix, conoy) + history)
            .toPersistentList()

    fun chatting(
        userStream: UserStream = yarrowStream,
        chatMessages: PersistentList<ChatListItem.Message> = this.chatMessages,
        recentEmotes: List<Emote> = this.recentEmotes,
    ) = ChatViewModel.State.Chatting(
        user = userStream.user,
        appUser = appUser,
        stream = userStream.stream,
        channelBadges = channelBadges,
        globalBadges = globalBadges,
        chatMessages = chatMessages,
        pronouns = pronouns,
        pickableEmotes = pickableEmotes,
        recentEmotes = recentEmotes,
        maxAdapterCount = 100,
        connectionStatus =
            ConnectionStatus(
                isAlive = true,
                registeredListeners = 1,
                aliveConnections = 1,
            ),
    )

    val recentEmoteSuggestions: ImmutableList<AutoCompleteItem> =
        recentEmotes.map { emote -> AutoCompleteItem.Emote(emote) }.toPersistentList()

    // Schedule

    private val today = JCLocalDate(LocalDate(2026, 9, 30))

    val schedule: ImmutableList<ScheduleDay> =
        persistentListOf(
            ScheduleDay(
                date = today,
                daysFromToday = 0,
                ongoing =
                    persistentListOf(
                        OngoingScheduleSegment(
                            segment =
                                ChannelScheduleSegment(
                                    id = "demo-segment-yarrow-live",
                                    user = yarrow,
                                    title = "Scieeeeence!",
                                    startTime = now - 40.minutes,
                                    endTime = now + 80.minutes,
                                    category = category,
                                ),
                            progress = 1f / 3f,
                        ),
                    ),
                upcoming = persistentListOf(),
            ),
            ScheduleDay(
                date = JCLocalDate(LocalDate(2026, 10, 1)),
                daysFromToday = 1,
                ongoing = persistentListOf(),
                upcoming =
                    persistentListOf(
                        ChannelScheduleSegment(
                            id = "demo-segment-yarrow",
                            user = yarrow,
                            title = "Tracking the approaching comet",
                            startTime = now + 1.days,
                            endTime = now + 1.days + 2.hours,
                            category = category,
                        ),
                    ),
            ),
            ScheduleDay(
                date = JCLocalDate(LocalDate(2026, 10, 2)),
                daysFromToday = 2,
                ongoing = persistentListOf(),
                upcoming =
                    persistentListOf(
                        ChannelScheduleSegment(
                            id = "demo-segment-poke",
                            user = poke,
                            title = "Searching for another way",
                            startTime = now + 2.days,
                            endTime = now + 2.days + 3.hours,
                            category = category,
                        ),
                    ),
            ),
        )
}
