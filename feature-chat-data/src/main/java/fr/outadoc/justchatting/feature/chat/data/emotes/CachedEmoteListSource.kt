package fr.outadoc.justchatting.feature.chat.data.emotes

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

abstract class CachedEmoteListSource : EmoteListSource {

    data class Params(
        val channelId: String,
        val channelName: String,
        val emoteSets: List<String>,
        val userId: String
    )

    private val inputFlow = MutableSharedFlow<Params>(replay = 1)

    private val cachedOutput: Flow<ImmutableSet<EmoteSetItem>> =
        inputFlow
            .distinctUntilChanged(::shouldUseCache)
            .map { params -> getEmotes(params) }

    override suspend fun getEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>,
        userId: String
    ): ImmutableSet<EmoteSetItem> {
        inputFlow.emit(
            Params(
                channelId = channelId,
                channelName = channelName,
                emoteSets = emoteSets,
                userId = userId
            )
        )

        return cachedOutput.catch { e -> throw e }.first()
    }

    abstract suspend fun getEmotes(params: Params): ImmutableSet<EmoteSetItem>
    abstract fun shouldUseCache(previous: Params, next: Params): Boolean
}