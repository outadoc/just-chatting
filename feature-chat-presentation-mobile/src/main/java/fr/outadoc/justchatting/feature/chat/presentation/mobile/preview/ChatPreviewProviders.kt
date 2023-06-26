package fr.outadoc.justchatting.feature.chat.presentation.mobile.preview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant

val previewBadges = listOf(
    "badge_subscriber_48",
    "badge_sub-gifter_100",
)

private val simpleEntries = sequence {
    yield(
        ChatEvent.Message.Simple(
            body = ChatEvent.Message.Body(
                chatter = Chatter(
                    displayName = "Hiccoz",
                    id = "68552712",
                    login = "hiccoz",
                ),
                message = "feur",
                messageId = "b43bd9e5-ec6e-47fe-a5da-c3213540fe06",
                isAction = false,
                color = "#FF69B4",
                embeddedEmotes = persistentListOf(),
                badges = persistentListOf(
                    fr.outadoc.justchatting.component.chatapi.common.Badge("subscriber", "48"),
                    fr.outadoc.justchatting.component.chatapi.common.Badge("sub-gifter", "100"),
                ),
                inReplyTo = null,
            ),
            timestamp = Instant.fromEpochMilliseconds(1664396374382),
        ),
    )

    yield(
        ChatEvent.Message.Simple(
            body = ChatEvent.Message.Body(
                chatter = Chatter(
                    displayName = "컬러히에",
                    id = "232421548",
                    login = "kolorye",
                ),
                message = "@djessy728 il avait dit quand mathieu avait sortit sa vidéo après longtemps qu'ils n'étaient plus en contact plus que ça, donc j'imagine que non",
                messageId = "4b3f4db7-5956-4ade-adba-ed282c22eb50",
                isAction = false,
                color = "#5F9EA0",
                embeddedEmotes = persistentListOf(),
                badges = persistentListOf(),
                inReplyTo = ChatEvent.Message.Body.InReplyTo(
                    id = "4d0a8518-9bc5-44a6-8249-c2ed9122f987",
                    chatter = Chatter(
                        id = "221570322",
                        displayName = "djessy728",
                        login = "djessy728",
                    ),
                    message = "Salut Antoine, est tu encore en contact avec Mathieu? Et penses tu streamer un peu avec lui?",
                ),
            ),
            timestamp = Instant.fromEpochMilliseconds(1664399217864),
        ),
    )
}

private val highlightedEntries = sequence {
    yield(
        ChatEvent.Message.Highlighted(
            timestamp = Instant.fromEpochMilliseconds(1664398268452),
            metadata = ChatEvent.Message.Highlighted.Metadata(
                title = "clo_chette_",
                titleIcon = Icons.Default.Star,
                subtitle = "subscribed at Tier 1. They've subscribed for 18 months!",
            ),
            body = ChatEvent.Message.Body(
                chatter = Chatter(
                    id = "672551946",
                    displayName = "clo_chette_",
                    login = "clo_chette_",
                ),
                message = "Top 1 on y croit",
                messageId = "9431957c-0185-4de8-91a6-d1734b733d90",
                isAction = false,
                color = "#8A2BE2",
                embeddedEmotes = persistentListOf(),
                badges = persistentListOf(),
                inReplyTo = null,
            ),
        ),
    )

    yield(
        ChatEvent.Message.Highlighted(
            timestamp = Instant.fromEpochMilliseconds(1664400523912),
            metadata = ChatEvent.Message.Highlighted.Metadata(
                title = "First message",
                titleIcon = Icons.Default.WavingHand,
                subtitle = null,
            ),
            body = ChatEvent.Message.Body(
                chatter = Chatter(
                    id = "0",
                    displayName = "Ravencheese",
                    login = "ravencheese",
                ),
                message = "Lezgooooo",
                messageId = "9431957c-0185-4de8-91a6-d1734b733d90",
                isAction = false,
                color = "#8a2be2",
                embeddedEmotes = persistentListOf(),
                badges = persistentListOf(),
                inReplyTo = null,
            ),
        ),
    )
}

private val paidEntries = sequence {
    yield(
        ChatEvent.Message.Highlighted(
            timestamp = Instant.parse("2023-06-26T17:53:11.208Z"),
            metadata = ChatEvent.Message.Highlighted.Metadata(
                title = "Boosted for $1",
                titleIcon = Icons.Default.Money,
                subtitle = null,
            ),
            body = ChatEvent.Message.Body(
                messageId = "e63c83f4-4f4c-44fb-b62d-b1003599e61a",
                chatter = Chatter(
                    id = "43868596",
                    login = "atyby",
                    displayName = "Atyby",
                ),
                message = "Everybody.",
                color = "#34BEED",
                isAction = false,
            ),
        ),
    )
}

private val noticeEntries = sequence {
    yield(
        ChatEvent.Message.Notice(
            timestamp = Instant.fromEpochMilliseconds(1664400523912),
            text = "This room is now in followers-only mode.",
        ),
    )

    yield(
        ChatEvent.Message.Notice(
            timestamp = Instant.fromEpochMilliseconds(1664400523912),
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
        ),
    )
}

class ChatMessagePreviewProvider : PreviewParameterProvider<ChatEvent.Message> {
    override val values: Sequence<ChatEvent.Message> = sequence {
        yieldAll(simpleEntries)
        yieldAll(noticeEntries)
        yieldAll(highlightedEntries)
        yieldAll(paidEntries)
    }
}

class SimpleChatMessagePreviewProvider : PreviewParameterProvider<ChatEvent.Message.Simple> {
    override val values: Sequence<ChatEvent.Message.Simple> = sequence {
        yieldAll(simpleEntries)
    }
}

class NoticeMessagePreviewProvider : PreviewParameterProvider<ChatEvent.Message.Notice> {
    override val values: Sequence<ChatEvent.Message.Notice> = sequence {
        yieldAll(noticeEntries)
    }
}
