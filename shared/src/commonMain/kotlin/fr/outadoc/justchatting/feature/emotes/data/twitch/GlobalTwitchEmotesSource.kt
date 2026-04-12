package fr.outadoc.justchatting.feature.emotes.data.twitch

import fr.outadoc.justchatting.feature.emotes.domain.CachedEmoteListSource
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.chat_source_twitch
import fr.outadoc.justchatting.utils.core.flatListOf
import fr.outadoc.justchatting.utils.resources.desc

internal class GlobalTwitchEmotesSource(
    private val delegateTwitchEmotesSource: DelegateTwitchEmotesSource,
) : CachedEmoteListSource<List<EmoteSetItem>>() {
    override fun shouldUseCache(
        previous: Params,
        next: Params,
    ): Boolean = false

    override suspend fun getEmotes(params: Params): Result<List<EmoteSetItem>> =
        delegateTwitchEmotesSource
            .getEmotes(
                channelId = params.channelId,
                channelName = params.channelName,
                emoteSets = params.emoteSets,
            ).map { emotes ->
                emotes.globalEmotes.flatMap { (owner, emotes) ->
                    flatListOf(
                        EmoteSetItem.Header(
                            title = owner?.displayName?.desc(),
                            source = Res.string.chat_source_twitch.desc(),
                            iconUrl = owner?.profileImageUrl,
                        ),
                        emotes.map { emote -> EmoteSetItem.Emote(emote) },
                    )
                }
            }
}
