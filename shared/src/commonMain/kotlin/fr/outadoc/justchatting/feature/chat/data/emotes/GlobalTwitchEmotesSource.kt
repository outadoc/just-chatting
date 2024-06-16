package fr.outadoc.justchatting.feature.chat.data.emotes

import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.chat_source_twitch
import fr.outadoc.justchatting.utils.core.flatListOf
import org.jetbrains.compose.resources.getString

class GlobalTwitchEmotesSource(
    private val delegateTwitchEmotesSource: DelegateTwitchEmotesSource,
) : CachedEmoteListSource<List<EmoteSetItem>>() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean = false

    override suspend fun getEmotes(params: Params): Result<List<EmoteSetItem>> =
        delegateTwitchEmotesSource
            .getEmotes(
                channelId = params.channelId,
                channelName = params.channelName,
                emoteSets = params.emoteSets,
            )
            .map { emotes ->
                emotes.globalEmotes.flatMap { (owner, emotes) ->
                    flatListOf(
                        EmoteSetItem.Header(
                            title = owner?.displayName,
                            source = getString(Res.string.chat_source_twitch),
                            iconUrl = owner?.profileImageUrl,
                        ),
                        emotes.map { emote -> EmoteSetItem.Emote(emote) },
                    )
                }
            }
}
