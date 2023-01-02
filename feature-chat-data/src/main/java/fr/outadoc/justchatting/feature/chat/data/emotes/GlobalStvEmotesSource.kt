package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.component.twitch.domain.repository.EmotesRepository
import fr.outadoc.justchatting.feature.chat.data.R
import fr.outadoc.justchatting.utils.core.asStringOrRes
import fr.outadoc.justchatting.utils.core.flatListOf

class GlobalStvEmotesSource(
    private val emotesRepository: EmotesRepository
) : CachedEmoteListSource() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean = true

    override suspend fun getEmotes(params: Params): List<EmoteSetItem> =
        flatListOf(
            EmoteSetItem.Header(
                title = null,
                source = R.string.chat_source_stv.asStringOrRes()
            ),
            emotesRepository.loadGlobalStvEmotes().emotes
                .map { emote -> EmoteSetItem.Emote(emote) }
        )
}