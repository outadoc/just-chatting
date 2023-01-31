package fr.outadoc.justchatting.feature.chat.presentation.mobile.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.feature.chat.presentation.mobile.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant

val previewBadges = listOf(
    "badge_subscriber_48",
    "badge_sub-gifter_100",
)

private val simpleEntries = sequence {
    yield(
        ChatEvent.Simple(
            data = ChatEvent.Data(
                userName = "Hiccoz",
                userId = "68552712",
                userLogin = "hiccoz",
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
        ChatEvent.Simple(
            data = ChatEvent.Data(
                userName = "컬러히에",
                userId = "232421548",
                userLogin = "kolorye",
                message = "@djessy728 il avait dit quand mathieu avait sortit sa vidéo après longtemps qu'ils n'étaient plus en contact plus que ça, donc j'imagine que non",
                messageId = "4b3f4db7-5956-4ade-adba-ed282c22eb50",
                isAction = false,
                color = "#5F9EA0",
                embeddedEmotes = persistentListOf(),
                badges = persistentListOf(),
                inReplyTo = ChatEvent.Data.InReplyTo(
                    id = "4d0a8518-9bc5-44a6-8249-c2ed9122f987",
                    userId = "221570322",
                    userName = "djessy728",
                    userLogin = "djessy728",
                    message = "Salut Antoine, est tu encore en contact avec Mathieu? Et penses tu streamer un peu avec lui?",
                ),
            ),
            timestamp = Instant.fromEpochMilliseconds(1664399217864),
        ),
    )
}

private val highlightedEntries = sequence {
    yield(
        ChatEvent.Highlighted(
            data = ChatEvent.Data(
                userName = "clo_chette_",
                userId = "672551946",
                userLogin = "clo_chette_",
                message = "Top 1 on y croit",
                messageId = "9431957c-0185-4de8-91a6-d1734b733d90",
                isAction = false,
                color = "#8A2BE2",
                embeddedEmotes = persistentListOf(),
                badges = persistentListOf(),
                inReplyTo = null,
            ),
            timestamp = Instant.fromEpochMilliseconds(1664398268452),
            header = "clo_chette_ subscribed at Tier 1. They've subscribed for 18 months!",
            headerIconResId = R.drawable.ic_star,
        ),
    )

    yield(
        ChatEvent.Highlighted(
            header = "This room is now in followers-only mode.",
            headerIconResId = null,
            data = null,
            timestamp = Instant.fromEpochMilliseconds(1664400523912),
        ),
    )
}

class ChatEntryPreviewProvider : PreviewParameterProvider<ChatEvent> {
    override val values: Sequence<ChatEvent> = sequence {
        yieldAll(simpleEntries)
        yieldAll(highlightedEntries)
    }
}
