package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DelegateTwitchEmotesSource(
    private val twitchRepository: TwitchRepository
) : CachedEmoteListSource<DelegateTwitchEmotesSource.CachedResult>() {

    data class CachedResult(
        val channelEmotes: Map<User?, List<TwitchEmote>>,
        val globalEmotes: Map<User?, List<TwitchEmote>>,
    )

    override fun shouldUseCache(previous: Params, next: Params): Boolean =
        previous.channelId == next.channelId && previous.emoteSets == next.emoteSets

    override suspend fun getEmotes(params: Params): CachedResult {
        return coroutineScope {
            val emotes: List<TwitchEmote> =
                params.emoteSets.chunked(25)
                    .map { setIds ->
                        async {
                            try {
                                twitchRepository.loadEmotesFromSet(setIds = setIds)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }.orEmpty()
                        }
                    }
                    .awaitAll()
                    .flatten()

            val emoteOwners: Map<String, User> =
                try {
                    twitchRepository.loadUsersById(
                        ids = emotes
                            .mapNotNull { emote -> emote.ownerId }
                            .toSet()
                            .mapNotNull { ownerId ->
                                ownerId.toLongOrNull()
                                    ?.takeIf { id -> id > 0 }
                                    ?.toString()
                            }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                    .orEmpty()
                    .associateBy { user -> user.id }

            CachedResult(
                channelEmotes = emotes.filter { emote -> emote.ownerId == params.channelId }
                    .groupBy { emoteOwners[params.channelId] },
                globalEmotes = emotes.filter { emote -> emote.ownerId != params.channelId }
                    .groupBy { emote -> emoteOwners[emote.ownerId] }
            )
        }
    }
}