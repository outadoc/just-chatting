package fr.outadoc.justchatting.preview

import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Raid
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import kotlinx.collections.immutable.persistentListOf
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
            token = "",
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
