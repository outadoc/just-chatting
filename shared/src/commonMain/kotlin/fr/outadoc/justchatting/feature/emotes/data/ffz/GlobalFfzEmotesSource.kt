package fr.outadoc.justchatting.feature.emotes.data.ffz

import fr.outadoc.justchatting.feature.emotes.domain.CachedEmoteListSource
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.chat_source_ffz
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.flatListOf
import fr.outadoc.justchatting.utils.resources.desc
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal class GlobalFfzEmotesSource(
    private val ffzEmotesApi: FfzEmotesApi,
    private val preferencesRepository: PreferenceRepository,
    private val dispatchersProvider: DispatchersProvider,
) : CachedEmoteListSource<List<EmoteSetItem>>() {
    override fun shouldUseCache(
        previous: Params,
        next: Params,
    ): Boolean = true

    override suspend fun getEmotes(params: Params): Result<List<EmoteSetItem>> =
        withContext(dispatchersProvider.io) {
            val prefs = preferencesRepository.currentPreferences.first()
            if (!prefs.enableFfzEmotes) {
                return@withContext Result.success(emptyList())
            }

            ffzEmotesApi
                .getGlobalFfzEmotes()
                .map { emotes ->
                    flatListOf(
                        EmoteSetItem.Header(
                            title = null,
                            source = Res.string.chat_source_ffz.desc(),
                        ),
                        emotes.map { emote -> EmoteSetItem.Emote(emote) },
                    )
                }
        }
}
