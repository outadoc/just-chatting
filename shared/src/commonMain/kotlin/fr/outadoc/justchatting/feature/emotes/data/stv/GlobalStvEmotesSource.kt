package fr.outadoc.justchatting.feature.emotes.data.stv

import fr.outadoc.justchatting.feature.emotes.domain.CachedEmoteListSource
import fr.outadoc.justchatting.feature.emotes.domain.model.EmoteSetItem
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.chat_source_stv
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.flatListOf
import fr.outadoc.justchatting.utils.resources.desc
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal class GlobalStvEmotesSource(
    private val stvEmotesApi: StvEmotesApi,
    private val preferencesRepository: PreferenceRepository,
) : CachedEmoteListSource<List<EmoteSetItem>>() {

    override fun shouldUseCache(previous: Params, next: Params): Boolean = true

    override suspend fun getEmotes(params: Params): Result<List<EmoteSetItem>> =
        withContext(DispatchersProvider.io) {
            val prefs = preferencesRepository.currentPreferences.first()
            if (!prefs.enableStvEmotes) {
                return@withContext Result.success(emptyList())
            }

            stvEmotesApi
                .getGlobalStvEmotes()
                .map { emotes ->
                    flatListOf(
                        EmoteSetItem.Header(
                            title = null,
                            source = Res.string.chat_source_stv.desc(),
                        ),
                        emotes.map { emote -> EmoteSetItem.Emote(emote) },
                    )
                }
        }
}
