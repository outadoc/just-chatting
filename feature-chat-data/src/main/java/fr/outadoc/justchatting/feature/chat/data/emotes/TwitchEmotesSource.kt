package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.component.twitch.domain.api.TwitchRepository
import fr.outadoc.justchatting.component.twitch.model.TwitchEmote
import fr.outadoc.justchatting.component.twitch.model.User
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class TwitchEmotesSource(private val twitchRepository: TwitchRepository) : CachedEmoteListSource() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean =
        previous.userId == next.userId && previous.emoteSets == next.emoteSets

    override suspend fun getEmotes(params: Params): ImmutableSet<EmoteSetItem> {
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

            val groupedChannelEmotes: Map<String?, List<TwitchEmote>> =
                emotes.filter { emote -> emote.ownerId == params.userId }
                    .groupBy { emoteOwners[params.userId]?.displayName }

            val groupedEmotes: Map<String?, List<TwitchEmote>> =
                emotes.filter { emote -> emote.ownerId != params.userId }
                    .groupBy { emote -> emoteOwners[emote.ownerId]?.displayName }

            val sortedEmotes: PersistentSet<EmoteSetItem> =
                (groupedChannelEmotes + groupedEmotes)
                    .flatMap { (ownerName, emotes) ->
                        listOf(EmoteSetItem.Header(title = ownerName, source = "Twitch"))
                            .plus(emotes.map { emote -> EmoteSetItem.Emote(emote) })
                    }
                    .toPersistentSet()

            sortedEmotes
        }
    }
}