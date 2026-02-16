package fr.outadoc.justchatting.feature.chat.presentation.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Icon
import fr.outadoc.justchatting.utils.resources.desc
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

internal val previewBadges =
    listOf(
        "badge_subscriber_48",
        "badge_sub-gifter_100",
    )

private val simpleEntries =
    sequence {
        yield(
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
                    embeddedEmotes = persistentListOf(),
                    badges =
                    persistentListOf(
                        Badge("subscriber", "48"),
                        Badge("sub-gifter", "100"),
                    ),
                    inReplyTo = null,
                ),
                timestamp = Instant.fromEpochMilliseconds(1664396374382),
            ),
        )

        yield(
            ChatListItem.Message.Simple(
                body =
                ChatListItem.Message.Body(
                    chatter =
                    Chatter(
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
                    inReplyTo =
                    ChatListItem.Message.Body.InReplyTo(
                        mentions = persistentListOf("djessy728"),
                        message = "Salut Antoine, est tu encore en contact avec Mathieu? Et penses tu streamer un peu avec lui?",
                    ),
                ),
                timestamp = Instant.fromEpochMilliseconds(1664399217864),
            ),
        )
    }

private val highlightedEntries =
    sequence {
        yield(
            ChatListItem.Message.Highlighted(
                timestamp = Instant.fromEpochMilliseconds(1664398268452),
                metadata =
                ChatListItem.Message.Highlighted.Metadata(
                    title = "clo_chette_".desc(),
                    titleIcon = Icon.Star,
                    subtitle = "subscribed at Tier 1. They've subscribed for 18 months!".desc(),
                ),
                body =
                ChatListItem.Message.Body(
                    chatter =
                    Chatter(
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
            ChatListItem.Message.Highlighted(
                timestamp = Instant.fromEpochMilliseconds(1664400523912),
                metadata =
                ChatListItem.Message.Highlighted.Metadata(
                    title = "First message".desc(),
                    titleIcon = Icon.WavingHand,
                    subtitle = null,
                ),
                body =
                ChatListItem.Message.Body(
                    chatter =
                    Chatter(
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

private val noticeEntries =
    sequence {
        yield(
            ChatListItem.Message.Notice(
                timestamp = Instant.fromEpochMilliseconds(1664400523912),
                text = "This room is now in followers-only mode.".desc(),
            ),
        )

        yield(
            ChatListItem.Message.Notice(
                timestamp = Instant.fromEpochMilliseconds(1664400523912),
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.".desc(),
            ),
        )
    }

internal class ChatMessagePreviewProvider : PreviewParameterProvider<ChatListItem.Message> {
    override val values: Sequence<ChatListItem.Message> =
        sequence {
            yieldAll(simpleEntries)
            yieldAll(noticeEntries)
            yieldAll(highlightedEntries)
        }
}

internal class SimpleChatMessagePreviewProvider : PreviewParameterProvider<ChatListItem.Message.Simple> {
    override val values: Sequence<ChatListItem.Message.Simple> =
        sequence {
            yieldAll(simpleEntries)
        }
}

internal class NoticeMessagePreviewProvider : PreviewParameterProvider<ChatListItem.Message.Notice> {
    override val values: Sequence<ChatListItem.Message.Notice> =
        sequence {
            yieldAll(noticeEntries)
        }
}
