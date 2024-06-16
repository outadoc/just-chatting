package fr.outadoc.justchatting.feature.chat.data.emotes

import dev.icerock.moko.resources.desc.desc
import fr.outadoc.justchatting.component.chatapi.domain.repository.EmotesRepository
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.flatListOf

class ChannelBttvEmotesSource(
    private val emotesRepository: EmotesRepository,
) : CachedEmoteListSource<List<EmoteSetItem>>() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean =
        previous.channelId == next.channelId && previous.channelName == next.channelName

    override suspend fun getEmotes(params: Params): Result<List<EmoteSetItem>> =
        emotesRepository
            .loadBttvEmotes(params.channelId)
            .map { emotes ->
                flatListOf(
                    EmoteSetItem.Header(
                        title = params.channelName.desc(),
                        source = MR.strings.chat_source_bttv.desc(),
                    ),
                    emotes.map { emote -> EmoteSetItem.Emote(emote) }
                )
            }
}
