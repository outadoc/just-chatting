package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.component.twitch.domain.repository.EmotesRepository

class ChannelStvEmotesSource(
    private val emotesRepository: EmotesRepository
) : CachedEmoteListSource() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean =
        previous.channelId == next.channelId && previous.channelName == next.channelName

    override suspend fun getEmotes(params: Params): List<EmoteSetItem> =
        flatListOf(
            EmoteSetItem.Header(title = params.channelName, source = "7TV"),
            emotesRepository.loadStvEmotes(params.channelId).emotes
                .map { emote -> EmoteSetItem.Emote(emote) }
        )
}