package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.emotes.domain.InsertRecentEmotesUseCase
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.RecentEmote
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.MessageNotSentException
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.chat_send_msg_error
import fr.outadoc.justchatting.utils.resources.desc
import kotlinx.collections.immutable.ImmutableMap
import kotlin.time.Clock

internal class SubmitMessageUseCase(
    private val clock: Clock,
    private val twitchRepository: TwitchRepository,
    private val insertRecentEmotes: InsertRecentEmotesUseCase,
) {
    suspend operator fun invoke(
        channelUserId: String,
        message: String,
        inReplyToMessageId: String?,
        appUser: AppUser.LoggedIn,
        allEmotesMap: ImmutableMap<String, Emote>,
        screenDensity: Float,
        isDarkTheme: Boolean,
    ): ChatViewModel.Action.AddMessages? {
        val currentTime = clock.now()

        val errorAction: ChatViewModel.Action.AddMessages? =
            twitchRepository
                .sendChatMessage(
                    channelUserId = channelUserId,
                    message = message,
                    inReplyToMessageId = inReplyToMessageId,
                    appUser = appUser,
                ).fold(
                    onSuccess = { null },
                    onFailure = { exception ->
                        ChatViewModel.Action.AddMessages(
                            listOf(
                                ChatListItem.Message.Highlighted(
                                    timestamp = clock.now(),
                                    metadata =
                                    ChatListItem.Message.Highlighted.Metadata(
                                        title = Res.string.chat_send_msg_error.desc(),
                                        subtitle =
                                        (exception as? MessageNotSentException)
                                            ?.dropReasonMessage
                                            ?.desc(),
                                    ),
                                    body = null,
                                ),
                            ),
                        )
                    },
                )

        val usedEmotes: List<RecentEmote> =
            message
                .split(' ')
                .mapNotNull { word ->
                    allEmotesMap[word]?.let { emote ->
                        RecentEmote(
                            name = word,
                            url =
                            emote.urls.getBestUrl(
                                screenDensity = screenDensity,
                                isDarkTheme = isDarkTheme,
                            ),
                            usedAt = currentTime,
                        )
                    }
                }

        insertRecentEmotes(usedEmotes)

        return errorAction
    }
}
