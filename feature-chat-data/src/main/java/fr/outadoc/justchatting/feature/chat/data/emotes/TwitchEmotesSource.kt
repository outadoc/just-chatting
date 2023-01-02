package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.component.twitch.domain.api.TwitchRepository
import fr.outadoc.justchatting.component.twitch.model.TwitchEmote
import fr.outadoc.justchatting.component.twitch.model.User
import fr.outadoc.justchatting.feature.chat.data.R
import fr.outadoc.justchatting.utils.core.asStringOrRes
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class TwitchEmotesSource(private val twitchRepository: TwitchRepository) : CachedEmoteListSource() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean =
        previous.channelId == next.channelId && previous.emoteSets == next.emoteSets

    override suspend fun getEmotes(params: Params): List<EmoteSetItem> {
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

            val groupedChannelEmotes: Map<User?, List<TwitchEmote>> =
                emotes.filter { emote -> emote.ownerId == params.channelId }
                    .groupBy { emoteOwners[params.channelId] }

            val groupedEmotes: Map<User?, List<TwitchEmote>> =
                emotes.filter { emote -> emote.ownerId != params.channelId }
                    .groupBy { emote -> emoteOwners[emote.ownerId] }

            val sortedEmotes: List<EmoteSetItem> =
                (groupedChannelEmotes + groupedEmotes)
                    .flatMap { (owner, emotes) ->
                        listOf(
                            EmoteSetItem.Header(
                                title = owner?.displayName?.asStringOrRes(),
                                source = R.string.chat_source_twitch.asStringOrRes(),
                                iconUrl = owner?.profileImageUrl
                            )
                        )
                            .plus(emotes.map { emote -> EmoteSetItem.Emote(emote) })
                    }

            sortedEmotes
        }
    }
}