package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.feature.chat.data.R
import fr.outadoc.justchatting.utils.core.asStringOrRes
import fr.outadoc.justchatting.utils.core.flatListOf

class ChannelTwitchEmotesSource(
    private val delegateTwitchEmotesSource: DelegateTwitchEmotesSource,
) : CachedEmoteListSource<List<EmoteSetItem>>() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean = false

    override suspend fun getEmotes(params: Params): List<EmoteSetItem> {
        val cache = delegateTwitchEmotesSource.getEmotes(
            channelId = params.channelId,
            channelName = params.channelName,
            emoteSets = params.emoteSets,
        )

        return cache.channelEmotes.flatMap { (owner, emotes) ->
            flatListOf(
                EmoteSetItem.Header(
                    title = owner?.displayName?.asStringOrRes(),
                    source = R.string.chat_source_twitch.asStringOrRes(),
                    iconUrl = owner?.profileImageUrl,
                ),
                emotes.map { emote -> EmoteSetItem.Emote(emote) },
            )
        }
    }
}