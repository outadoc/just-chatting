package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.component.chatapi.domain.repository.EmotesRepository
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.chat_source_ffz
import fr.outadoc.justchatting.utils.core.flatListOf
import org.jetbrains.compose.resources.getString

class GlobalFfzEmotesSource(
    private val emotesRepository: EmotesRepository,
) : CachedEmoteListSource<List<EmoteSetItem>>() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean = true

    override suspend fun getEmotes(params: Params): Result<List<EmoteSetItem>> =
        emotesRepository
            .loadBttvGlobalFfzEmotes()
            .map { emotes ->
                flatListOf(
                    EmoteSetItem.Header(
                        title = null,
                        source = getString(Res.string.chat_source_ffz),
                    ),
                    emotes.map { emote -> EmoteSetItem.Emote(emote) },
                )
            }
}
