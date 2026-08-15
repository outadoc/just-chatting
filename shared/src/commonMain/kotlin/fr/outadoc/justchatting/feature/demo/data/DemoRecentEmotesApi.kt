package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.emotes.domain.RecentEmotesApi
import fr.outadoc.justchatting.feature.emotes.domain.model.RecentEmote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DemoRecentEmotesApi : RecentEmotesApi {
    private val recentEmotes = MutableStateFlow<List<RecentEmote>>(emptyList())

    override fun getAll(): Flow<List<RecentEmote>> = recentEmotes

    override fun insertAll(emotes: Collection<RecentEmote>) {
        val byName = recentEmotes.value.associateBy { it.name } + emotes.associateBy { it.name }
        recentEmotes.value = byName.values.sortedByDescending { it.usedAt }
    }
}
