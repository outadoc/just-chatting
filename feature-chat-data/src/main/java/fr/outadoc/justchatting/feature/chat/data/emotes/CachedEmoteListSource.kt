package fr.outadoc.justchatting.feature.chat.data.emotes

abstract class CachedEmoteListSource : EmoteListSource {

    data class Params(
        val channelId: String,
        val channelName: String,
        val emoteSets: List<String>
    )

    private var cachedResult: Pair<Params, List<EmoteSetItem>>? = null

    override suspend fun getEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>
    ): List<EmoteSetItem> {
        val params = Params(
            channelId = channelId,
            channelName = channelName,
            emoteSets = emoteSets
        )

        val cachedResult = this.cachedResult
        return if (cachedResult != null && shouldUseCache(cachedResult.first, params)) {
            cachedResult.second
        } else {
            getEmotes(params).also { result ->
                this.cachedResult = params to result
            }
        }
    }

    abstract suspend fun getEmotes(params: Params): List<EmoteSetItem>
    abstract fun shouldUseCache(previous: Params, next: Params): Boolean
}