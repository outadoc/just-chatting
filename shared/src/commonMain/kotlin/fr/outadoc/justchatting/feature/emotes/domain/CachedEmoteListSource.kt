package fr.outadoc.justchatting.feature.emotes.domain

internal abstract class CachedEmoteListSource<T> : EmoteListSource<T> {

    data class Params(
        val channelId: String,
        val channelName: String,
        val emoteSets: List<String>,
    )

    private var cachedResult: Pair<Params, T>? = null

    override suspend fun getEmotes(
        channelId: String,
        channelName: String,
        emoteSets: List<String>,
    ): Result<T> {
        val params = Params(
            channelId = channelId,
            channelName = channelName,
            emoteSets = emoteSets,
        )

        val cachedResult = this.cachedResult
        return if (cachedResult != null && shouldUseCache(cachedResult.first, params)) {
            Result.success(cachedResult.second)
        } else {
            getEmotes(params)
                .onSuccess { result ->
                    this.cachedResult = params to result
                }
        }
    }

    abstract suspend fun getEmotes(params: Params): Result<T>
    abstract fun shouldUseCache(previous: Params, next: Params): Boolean
}
