package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.component.chatapi.domain.repository.EmotesRepository
import fr.outadoc.justchatting.feature.chat.data.MR
import fr.outadoc.justchatting.utils.core.asStringOrRes
import fr.outadoc.justchatting.utils.core.flatListOf

class GlobalBttvEmotesSource(
    private val emotesRepository: EmotesRepository,
) : CachedEmoteListSource<List<EmoteSetItem>>() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean = true

    override suspend fun getEmotes(params: Params): List<EmoteSetItem> =
        flatListOf(
            EmoteSetItem.Header(
                title = null,
                source = MR.strings.chat_source_bttv.asStringOrRes(),
            ),
            emotesRepository.loadGlobalBttvEmotes()
                .map { emote -> EmoteSetItem.Emote(emote) },
        )
}
