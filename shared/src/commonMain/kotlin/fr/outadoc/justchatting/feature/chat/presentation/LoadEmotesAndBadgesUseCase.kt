package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.domain.EmoteListSourcesProvider
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentHashMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class LoadEmotesAndBadgesUseCase(
    private val twitchRepository: TwitchRepository,
    private val emoteListSourcesProvider: EmoteListSourcesProvider,
) {
    suspend operator fun invoke(
        channelId: String,
        channelName: String,
        emoteSets: List<String>,
    ): ChatViewModel.Action.UpdateEmotes = coroutineScope {
        val globalBadges = async {
            twitchRepository
                .getGlobalBadges()
                .fold(
                    onSuccess = { badges -> badges.toPersistentList() },
                    onFailure = { exception ->
                        logError<LoadEmotesAndBadgesUseCase>(exception) { "Failed to load global badges" }
                        null
                    },
                )
        }

        val channelBadges = async {
            twitchRepository
                .getChannelBadges(channelId)
                .fold(
                    onSuccess = { badges -> badges.toPersistentList() },
                    onFailure = { exception ->
                        logError<LoadEmotesAndBadgesUseCase>(exception) { "Failed to load badges for channel $channelId" }
                        null
                    },
                )
        }

        val cheerEmotes: PersistentMap<String, Emote>? =
            twitchRepository
                .getCheerEmotes(userId = channelId)
                .fold(
                    onSuccess = { emotes ->
                        emotes
                            .associateBy { emote -> emote.name }
                            .toPersistentHashMap()
                    },
                    onFailure = { exception ->
                        logError<LoadEmotesAndBadgesUseCase>(exception) { "Failed to load cheer emotes for channel $channelId" }
                        null
                    },
                )

        val pickableEmotes: PersistentList<EmoteSetItem> =
            loadPickableEmotes(
                channelId = channelId,
                channelName = channelName,
                emoteSets = emoteSets,
            )

        ChatViewModel.Action.UpdateEmotes(
            pickableEmotes = pickableEmotes,
            globalBadges = globalBadges.await(),
            channelBadges = channelBadges.await(),
            cheerEmotes = cheerEmotes,
        )
    }

    private suspend fun loadPickableEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>,
    ): PersistentList<EmoteSetItem> = coroutineScope {
        emoteListSourcesProvider
            .getSources()
            .map { source ->
                async {
                    source
                        .getEmotes(
                            channelId = channelId,
                            channelName = channelName,
                            emoteSets = emoteSets,
                        ).fold(
                            onSuccess = { emotes -> emotes },
                            onFailure = { exception ->
                                logError<LoadEmotesAndBadgesUseCase>(exception) { "Failed to load emotes from source $source" }
                                emptyList()
                            },
                        )
                }
            }.awaitAll()
            .flatten()
            .toPersistentList()
    }
}
