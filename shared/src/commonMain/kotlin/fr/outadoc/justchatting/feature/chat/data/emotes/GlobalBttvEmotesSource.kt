package fr.outadoc.justchatting.feature.chat.data.emotes

import dev.icerock.moko.resources.desc.desc
import fr.outadoc.justchatting.component.chatapi.domain.repository.EmotesRepository
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.utils.core.flatListOf

class GlobalBttvEmotesSource(
    private val emotesRepository: EmotesRepository,
) : CachedEmoteListSource<List<EmoteSetItem>>() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean = true

    override suspend fun getEmotes(params: Params): Result<List<EmoteSetItem>> =
        emotesRepository
            .loadGlobalBttvEmotes()
            .map { emotes ->
                flatListOf(
                    EmoteSetItem.Header(
                        title = null,
                        source = Res.string.chat_source_bttv.desc(),
                    ),
                    emotes.map { emote -> EmoteSetItem.Emote(emote) },
                )
            }
}
