package fr.outadoc.justchatting.preview

import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteUrls
import fr.outadoc.justchatting.feature.preferences.domain.model.ApiToken
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Shared sample data for Compose screenshot tests. Names describe the role a value plays
 * in a test (e.g. "the sample user"), not its specific content, so they stay accurate if the
 * underlying literal is ever swapped out.
 */
internal object PreviewFixtures {
    // A fixed clock keeps rendered "time since" durations stable across the update/validate
    // Gradle runs, instead of ticking with the real system clock.
    val fixedClock: Clock =
        object : Clock {
            override fun now(): Instant = Instant.parse("2022-01-01T18:00:00Z")
        }

    // A fixed time zone keeps rendered times stable whatever the zone of the machine running
    // the tests.
    val timeZone: TimeZone = TimeZone.UTC

    const val sampleTextShort: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    const val sampleTextLong: String =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin."

    fun user(
        id: String,
        login: String,
        displayName: String,
        description: String = "",
        profileImageUrl: String = "",
        createdAt: Instant = Instant.DISTANT_PAST,
        usedAt: Instant? = Instant.DISTANT_PAST,
    ): User =
        User(
            id = id,
            login = login,
            displayName = displayName,
            description = description,
            profileImageUrl = profileImageUrl,
            createdAt = createdAt,
            usedAt = usedAt,
        )

    val sampleUser: User = user(id = "1", login = "maghla", displayName = "Maghla")

    val sampleLoggedInUser: AppUser.LoggedIn =
        AppUser.LoggedIn(
            userId = "123",
            userLogin = "outadoc",
            token = ApiToken(""),
        )

    // A logged-in user whose login is as long as Twitch allows (25 characters).
    val sampleLoggedInUserWithLongLogin: AppUser.LoggedIn =
        AppUser.LoggedIn(
            userId = "456",
            userLogin = "outadoc_has_a_long_login1",
            token = ApiToken(""),
        )

    val sampleChatMessage: ChatListItem.Message.Simple =
        ChatListItem.Message.Simple(
            body =
                ChatListItem.Message.Body(
                    chatter =
                        Chatter(
                            displayName = "Hiccoz",
                            id = "68552712",
                            login = "hiccoz",
                        ),
                    message = "feur",
                    messageId = "b43bd9e5-ec6e-47fe-a5da-c3213540fe06",
                    isAction = false,
                    color = "#FF69B4",
                    badges =
                        persistentListOf(
                            Badge("subscriber", "48"),
                            Badge("sub-gifter", "100"),
                        ),
                ),
            timestamp = Instant.fromEpochMilliseconds(1664396374382),
        )

    // A message mentioning both the sample logged-in user and another chatter,
    // so only the first mention gets highlighted.
    val sampleMentionMessage: ChatListItem.Message.Simple =
        ChatListItem.Message.Simple(
            body =
                ChatListItem.Message.Body(
                    chatter =
                        Chatter(
                            displayName = "Hiccoz",
                            id = "68552712",
                            login = "hiccoz",
                        ),
                    message = "@outadoc tu peux mod @marion_11 stp ?",
                    messageId = "5d0f3a3e-7b8e-4c55-9a43-2f1a4d6c7e10",
                    color = "#FF69B4",
                ),
            timestamp = Instant.fromEpochMilliseconds(1664396374382),
        )

    // A message mentioning a chatter other than the sample logged-in user,
    // so the mention is only emboldened, without any highlight.
    val sampleOtherUserMentionMessage: ChatListItem.Message.Simple =
        ChatListItem.Message.Simple(
            body =
                ChatListItem.Message.Body(
                    chatter =
                        Chatter(
                            displayName = "Hiccoz",
                            id = "68552712",
                            login = "hiccoz",
                        ),
                    message = "@marion_11 tu peux mod @hiccoz stp ?",
                    messageId = "3e7a1c94-2b6f-4d0e-a8c5-6f9b2d4e1a73",
                    color = "#FF69B4",
                ),
            timestamp = Instant.fromEpochMilliseconds(1664396374382),
        )

    // A message mentioning the logged-in user with a long login, so that
    // the mention wraps over two lines in a narrow layout.
    val sampleLongMentionMessage: ChatListItem.Message.Simple =
        ChatListItem.Message.Simple(
            body =
                ChatListItem.Message.Body(
                    chatter =
                        Chatter(
                            displayName = "Hiccoz",
                            id = "68552712",
                            login = "hiccoz",
                        ),
                    message = "salut @outadoc_has_a_long_login1 tu peux mod stp ?",
                    messageId = "c41d8e27-6a3b-4f95-b0e2-7d9a1f6c3b58",
                    color = "#FF69B4",
                ),
            timestamp = Instant.fromEpochMilliseconds(1664396374382),
        )

    // A reply to a message sent by the sample logged-in user.
    val sampleReplyToAppUserMessage: ChatListItem.Message.Simple =
        ChatListItem.Message.Simple(
            body =
                ChatListItem.Message.Body(
                    chatter =
                        Chatter(
                            displayName = "marion_11",
                            id = "280065659",
                            login = "marion_11",
                        ),
                    message = "bonjour",
                    messageId = "9b2c6e1f-4d8a-4f3b-8e6d-1c7a5b3f2e90",
                    color = "#1E90FF",
                    inReplyTo =
                        ChatListItem.Message.Body.InReplyTo(
                            message = "test de test",
                            mentions = persistentListOf("outadoc"),
                        ),
                ),
            timestamp = Instant.fromEpochMilliseconds(1664396674382),
        )

    // A reply to a long message sent by the sample logged-in user, so that the
    // reply header wraps under the mention and gets cut off at its max lines.
    val sampleReplyToLongAppUserMessage: ChatListItem.Message.Simple =
        ChatListItem.Message.Simple(
            body =
                ChatListItem.Message.Body(
                    chatter =
                        Chatter(
                            displayName = "marion_11",
                            id = "280065659",
                            login = "marion_11",
                        ),
                    message = "oui bien sûr",
                    messageId = "7f4e2a9c-1b6d-4c3e-8a5f-2e9d7b1c6a38",
                    color = "#1E90FF",
                    inReplyTo =
                        ChatListItem.Message.Body.InReplyTo(
                            message =
                                "est-ce que quelqu'un pourrait me dire à quelle heure commence " +
                                    "le stream demain ? je voudrais pas rater le début, " +
                                    "surtout s'il y a des annonces importantes",
                            mentions = persistentListOf("outadoc"),
                        ),
                ),
            timestamp = Instant.fromEpochMilliseconds(1664396674382),
        )

    // A message whose last emote is enlarged, and drawn on its own line.
    val sampleGigantifiedEmoteMessage: ChatListItem.Message.Simple =
        ChatListItem.Message.Simple(
            body =
                ChatListItem.Message.Body(
                    chatter =
                        Chatter(
                            displayName = "Ala1nPr0ust",
                            id = "30983168",
                            login = "ala1npr0ust",
                        ),
                    message = "pas de prime dispo mais j'offre un gros panard du coup",
                    messageId = "fb3ae984-2740-4241-862d-2289e791dae4",
                    color = "#F7C4CD",
                    gigantifiedEmote =
                        Emote(
                            name = "dfgTimide",
                            urls = EmoteUrls(url = "https://example.com/dfgTimide.png"),
                        ),
                ),
            timestamp = Instant.fromEpochMilliseconds(1786969269497),
        )

    // A second, distinct message so chat-list previews can show more than one item.
    val sampleChatMessageAlternate: ChatListItem.Message.Simple =
        ChatListItem.Message.Simple(
            body =
                ChatListItem.Message.Body(
                    chatter =
                        Chatter(
                            displayName = "marion_11",
                            id = "280065659",
                            login = "marion_11",
                        ),
                    message = "ok att jen ai un comme vous",
                    messageId = "a5d36b3a-f663-4890-9d82-cbf1f89ce726",
                    isAction = false,
                    color = "#FF0000",
                ),
            timestamp = Instant.fromEpochMilliseconds(1664399218000),
        )

    // Declared after sampleTextShort/sampleUser above: object properties initialize
    // top-to-bottom, and sampleStream reads both of them.
    val sampleStreamCategory: StreamCategory = StreamCategory(id = "1", name = "Powerwash Simulator")
    val sampleTimestamp: Instant = Instant.parse("2022-01-01T13:45:04.00Z")
    val sampleStream: Stream =
        Stream(
            id = "1",
            userId = sampleUser.id,
            category = sampleStreamCategory,
            title = sampleTextShort,
            viewerCount = 5_305,
            startedAt = sampleTimestamp,
        )

    val sampleChatter: Chatter =
        Chatter(
            id = "1",
            displayName = "BagheraJones",
            login = "bagherajones",
        )

    // Named by the domain state they represent, not the raid target's name.
    val sampleRaidGo: Raid.Go =
        Raid.Go(
            targetId = "",
            targetLogin = "",
            targetDisplayName = "HortyUnderscore",
            targetProfileImageUrl = null,
            viewerCount = 12_000,
        )

    val sampleRaidPreparing: Raid.Preparing =
        Raid.Preparing(
            targetId = "",
            targetLogin = "",
            targetDisplayName = "HortyUnderscore",
            targetProfileImageUrl = null,
            viewerCount = 12_000,
        )
}
