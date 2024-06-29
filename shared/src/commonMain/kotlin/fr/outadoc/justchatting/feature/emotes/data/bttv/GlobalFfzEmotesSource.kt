package fr.outadoc.justchatting.feature.emotes.data.bttv

import dev.icerock.moko.resources.desc.desc
import fr.outadoc.justchatting.feature.emotes.domain.CachedEmoteListSource
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.flatListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal class GlobalFfzEmotesSource(
    private val bttvEmotesApi: BttvEmotesApi,
    private val preferencesRepository: PreferenceRepository,
) : CachedEmoteListSource<List<EmoteSetItem>>() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean = true

    override suspend fun getEmotes(params: Params): Result<List<EmoteSetItem>> =
        withContext(DispatchersProvider.io) {
            if (!preferencesRepository.currentPreferences.first().enableFfzEmotes) {
                return@withContext Result.success(emptyList())
            }

            bttvEmotesApi
                .getBttvGlobalFfzEmotes()
                .map { emotes ->
                    flatListOf(
                        EmoteSetItem.Header(
                            title = null,
                            source = MR.strings.chat_source_ffz.desc(),
                        ),
                        emotes.map { emote -> EmoteSetItem.Emote(emote) },
                    )
                }
        }
}
