package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.component.twitch.domain.repository.EmotesRepository

class GlobalStvEmotesSource(
    private val emotesRepository: EmotesRepository
) : CachedEmoteListSource() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean = true

    override suspend fun getEmotes(params: Params): List<EmoteSetItem> =
        flatListOf(
            EmoteSetItem.Header(title = null, source = "7TV"),
            emotesRepository.loadGlobalStvEmotes().emotes
                .map { emote -> EmoteSetItem.Emote(emote) }
        )
}