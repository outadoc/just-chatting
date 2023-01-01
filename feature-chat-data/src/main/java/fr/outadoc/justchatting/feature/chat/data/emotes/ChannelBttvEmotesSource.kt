package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.component.twitch.domain.repository.EmotesRepository
import kotlinx.collections.immutable.ImmutableSet

class ChannelBttvEmotesSource(
    private val emotesRepository: EmotesRepository
) : CachedEmoteListSource() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean =
        previous.channelId == next.channelId && previous.channelName == next.channelName

    override suspend fun getEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>,
        userId: String
    ): ImmutableSet<EmoteSetItem> =
        flatImmutableSetOf(
            EmoteSetItem.Header(title = channelName, source = "BetterTTV"),
            emotesRepository.loadBttvEmotes(channelId).emotes
                .map { emote -> EmoteSetItem.Emote(emote) }
        )

    override suspend fun getEmotes(params: Params): ImmutableSet<EmoteSetItem> =
        flatImmutableSetOf(
            EmoteSetItem.Header(title = params.channelName, source = "BetterTTV"),
            emotesRepository.loadBttvEmotes(params.channelId).emotes
                .map { emote -> EmoteSetItem.Emote(emote) }
        )
}