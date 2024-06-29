package fr.outadoc.justchatting.feature.emotes.domain

import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.home.domain.TwitchRepository
import fr.outadoc.justchatting.feature.home.domain.model.User
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class DelegateTwitchEmotesSource(
    private val twitchRepository: TwitchRepository,
) : CachedEmoteListSource<DelegateTwitchEmotesSource.CachedResult>() {

    data class CachedResult(
        val channelEmotes: Map<User?, List<Emote>>,
        val globalEmotes: Map<User?, List<Emote>>,
    )

    override fun shouldUseCache(previous: Params, next: Params): Boolean =
        previous.channelId == next.channelId && previous.emoteSets == next.emoteSets

    override suspend fun getEmotes(params: Params): Result<CachedResult> {
        return coroutineScope {
            val emotes: List<Emote> =
                params.emoteSets
                    .chunked(25)
                    .map { setIds ->
                        async {
                            twitchRepository
                                .loadEmotesFromSet(setIds = setIds)
                                .fold(
                                    onSuccess = { emotes -> emotes },
                                    onFailure = { exception ->
                                        logError<DelegateTwitchEmotesSource>(exception) { "Failed to load Twitch emotes for setIds" }
                                        emptyList()
                                    },
                                )
                        }
                    }
                    .awaitAll()
                    .flatten()

            val emoteOwners: Map<String, User> =
                twitchRepository
                    .loadUsersById(
                        ids = emotes
                            .mapNotNull { emote -> emote.ownerId }
                            .toSet()
                            .mapNotNull { ownerId ->
                                ownerId.toLongOrNull()
                                    ?.takeIf { id -> id > 0 }
                                    ?.toString()
                            },
                    )
                    .fold(
                        onSuccess = { users -> users },
                        onFailure = { exception ->
                            logError<DelegateTwitchEmotesSource>(exception) { "Failed to load Twitch emote owners" }
                            emptyList()
                        },
                    )
                    .associateBy { user -> user.id }

            Result.success(
                CachedResult(
                    channelEmotes = emotes.filter { emote -> emote.ownerId == params.channelId }
                        .groupBy { emoteOwners[params.channelId] },
                    globalEmotes = emotes.filter { emote -> emote.ownerId != params.channelId }
                        .groupBy { emote -> emoteOwners[emote.ownerId] },
                ),
            )
        }
    }
}
