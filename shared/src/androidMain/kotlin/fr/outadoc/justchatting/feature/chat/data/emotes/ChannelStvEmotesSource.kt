package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.component.chatapi.domain.repository.EmotesRepository
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.asStringOrRes
import fr.outadoc.justchatting.utils.core.flatListOf

class ChannelStvEmotesSource(
    private val emotesRepository: EmotesRepository,
) : CachedEmoteListSource<List<EmoteSetItem>>() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean =
        previous.channelId == next.channelId && previous.channelName == next.channelName

    override suspend fun getEmotes(params: Params): List<EmoteSetItem> =
        flatListOf(
            EmoteSetItem.Header(
                title = params.channelName.asStringOrRes(),
                source = MR.strings.chat_source_stv.asStringOrRes(),
            ),
            emotesRepository.loadStvEmotes(params.channelId)
                .map { emote -> EmoteSetItem.Emote(emote) },
        )
}
