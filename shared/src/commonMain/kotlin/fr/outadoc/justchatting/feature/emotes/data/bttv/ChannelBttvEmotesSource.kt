package fr.outadoc.justchatting.feature.emotes.data.bttv

import fr.outadoc.justchatting.feature.emotes.domain.CachedEmoteListSource
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.chat_source_bttv
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.flatListOf
import fr.outadoc.justchatting.utils.resources.desc
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal class ChannelBttvEmotesSource(
    private val bttvEmotesApi: BttvEmotesApi,
    private val preferencesRepository: PreferenceRepository,
) : CachedEmoteListSource<List<EmoteSetItem>>() {
    override fun shouldUseCache(
        previous: Params,
        next: Params,
    ): Boolean = previous.channelId == next.channelId && previous.channelName == next.channelName

    override suspend fun getEmotes(params: Params): Result<List<EmoteSetItem>> =
        withContext(
            DispatchersProvider.io,
        ) {
            val prefs = preferencesRepository.currentPreferences.first()
            if (!prefs.enableBttvEmotes) {
                return@withContext Result.success(emptyList())
            }

            bttvEmotesApi
                .getBttvEmotes(params.channelId)
                .map { emotes ->
                    flatListOf(
                        EmoteSetItem.Header(
                            title = params.channelName.desc(),
                            source = Res.string.chat_source_bttv.desc(),
                        ),
                        emotes.map { emote -> EmoteSetItem.Emote(emote) },
                    )
                }
        }
}
