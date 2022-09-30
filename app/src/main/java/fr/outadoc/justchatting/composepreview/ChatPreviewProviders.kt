package fr.outadoc.justchatting.composepreview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.chat.Badge
import fr.outadoc.justchatting.model.chat.ChatMessage
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant

@Preview(group = "themes", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(group = "themes", uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class ThemePreviews

val previewBadges = listOf(
    "badge_subscriber_48",
    "badge_sub-gifter_100"
)

private val simpleEntries = sequence {
    yield(
        ChatEntry.Simple(
            data = ChatEntry.Data(
                userName = "Hiccoz",
                userId = "68552712",
                userLogin = "hiccoz",
                message = "feur",
                messageId = "b43bd9e5-ec6e-47fe-a5da-c3213540fe06",
                isAction = false,
                color = "#FF69B4",
                emotes = persistentListOf(),
                badges = persistentListOf(
                    Badge("subscriber", "48"),
                    Badge("sub-gifter", "100")
                ),
                inReplyTo = null
            ),
            timestamp = Instant.fromEpochMilliseconds(1664396374382)
        )
    )

    yield(
        ChatEntry.Simple(
            data = ChatEntry.Data(
                userName = "컬러히에",
                userId = "232421548",
                userLogin = "kolorye",
                message = "@djessy728 il avait dit quand mathieu avait sortit sa vidéo après longtemps qu'ils n'étaient plus en contact plus que ça, donc j'imagine que non",
                messageId = "4b3f4db7-5956-4ade-adba-ed282c22eb50",
                isAction = false,
                color = "#5F9EA0",
                emotes = persistentListOf(),
                badges = persistentListOf(),
                inReplyTo = ChatMessage.InReplyTo(
                    id = "4d0a8518-9bc5-44a6-8249-c2ed9122f987",
                    userId = "221570322",
                    userName = "djessy728",
                    userLogin = "djessy728",
                    message = "Salut Antoine, est tu encore en contact avec Mathieu? Et penses tu streamer un peu avec lui?"
                )
            ),
            timestamp = Instant.fromEpochMilliseconds(1664399217864)
        )
    )
}

private val highlightedEntries = sequence {
    yield(
        ChatEntry.Highlighted(
            data = ChatEntry.Data(
                userName = "clo_chette_",
                userId = "672551946",
                userLogin = "clo_chette_",
                message = "Top 1 on y croit",
                messageId = "9431957c-0185-4de8-91a6-d1734b733d90",
                isAction = false,
                color = "#8A2BE2",
                emotes = persistentListOf(),
                badges = persistentListOf(),
                inReplyTo = null
            ),
            timestamp = Instant.fromEpochMilliseconds(1664398268452),
            header = "clo_chette_ subscribed at Tier 1. They've subscribed for 18 months!",
            headerIconResId = R.drawable.ic_star
        )
    )

    yield(
        ChatEntry.Highlighted(
            header = "This room is now in followers-only mode.",
            headerIconResId = null,
            data = null,
            timestamp = Instant.fromEpochMilliseconds(1664400523912)
        )
    )
}

class ChatEntryPreviewProvider : PreviewParameterProvider<ChatEntry> {
    override val values: Sequence<ChatEntry> = sequence {
        yieldAll(simpleEntries)
        yieldAll(highlightedEntries)
    }
}
