package com.github.andreyasadchy.xtra.repository.datasource

import androidx.core.util.Pair
import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.FollowedVideosQuery
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientWithTokenFactory.apolloClientWithToken
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.GraphQLRepository
import com.github.andreyasadchy.xtra.type.BroadcastType
import com.github.andreyasadchy.xtra.type.VideoSort
import com.github.andreyasadchy.xtra.util.C
import kotlinx.coroutines.CoroutineScope

class FollowedVideosDataSource(
    private val userId: String?,
    private val gqlClientId: String?,
    private val gqlToken: String?,
    private val gqlQueryType: BroadcastType?,
    private val gqlQuerySort: VideoSort?,
    private val gqlApi: GraphQLRepository,
    private val apiPref: ArrayList<Pair<Long?, String?>?>,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Video>(coroutineScope) {
    private var api: String? = null
    private var offset: String? = null
    private var nextPage: Boolean = true

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Video>) {
        loadInitial(params, callback) {
            try {
                when (apiPref.elementAt(0)?.second) {
                    C.GQL_QUERY -> if (!gqlToken.isNullOrBlank()) gqlQueryInitial(params) else throw Exception()
                    C.GQL -> if (!gqlToken.isNullOrBlank() && gqlQueryType == BroadcastType.ARCHIVE && gqlQuerySort == VideoSort.TIME) gqlInitial(params) else throw Exception()
                    else -> mutableListOf()
                }
            } catch (e: Exception) {
                try {
                    when (apiPref.elementAt(1)?.second) {
                        C.GQL_QUERY -> if (!gqlToken.isNullOrBlank()) gqlQueryInitial(params) else throw Exception()
                        C.GQL -> if (!gqlToken.isNullOrBlank() && gqlQueryType == BroadcastType.ARCHIVE && gqlQuerySort == VideoSort.TIME) gqlInitial(params) else throw Exception()
                        else -> mutableListOf()
                    }
                } catch (e: Exception) {
                    mutableListOf()
                }
            }
        }
    }

    private suspend fun gqlQueryInitial(params: LoadInitialParams): List<Video> {
        api = C.GQL_QUERY
        val typeList = if (gqlQueryType != null) mutableListOf(gqlQueryType) else null
        val get1 = apolloClientWithToken(XtraModule(), gqlClientId, gqlToken)
            .query(FollowedVideosQuery(id = Optional.Present(userId), sort = Optional.Present(gqlQuerySort), type = Optional.Present(typeList), first = Optional.Present(params.requestedLoadSize), after = Optional.Present(offset))).execute().data?.user?.followedVideos
        val get = get1?.edges
        val list = mutableListOf<Video>()
        if (get != null) {
            for (i in get) {
                list.add(
                    Video(
                        id = i?.node?.id ?: "",
                        user_id = i?.node?.owner?.id,
                        user_login = i?.node?.owner?.login,
                        user_name = i?.node?.owner?.displayName,
                        gameId = i?.node?.game?.id,
                        gameName = i?.node?.game?.displayName,
                        type = i?.node?.broadcastType.toString(),
                        title = i?.node?.title,
                        view_count = i?.node?.viewCount,
                        createdAt = i?.node?.createdAt,
                        duration = i?.node?.lengthSeconds.toString(),
                        thumbnail_url = i?.node?.previewThumbnailURL,
                        profileImageURL = i?.node?.owner?.profileImageURL,
                    )
                )
            }
            offset = get.lastOrNull()?.cursor
            nextPage = get1.pageInfo?.hasNextPage ?: true
        }
        return list
    }

    private suspend fun gqlInitial(params: LoadInitialParams): List<Video> {
        api = C.GQL
        val get = gqlApi.loadFollowedVideos(gqlClientId, gqlToken, params.requestedLoadSize, offset)
        offset = get.cursor
        return get.data
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Video>) {
        loadRange(params, callback) {
            when (api) {
                C.GQL_QUERY -> gqlQueryRange(params)
                C.GQL -> gqlRange(params)
                else -> mutableListOf()
            }
        }
    }

    private suspend fun gqlQueryRange(params: LoadRangeParams): List<Video> {
        val typeList = if (gqlQueryType != null) mutableListOf(gqlQueryType) else null
        val get1 = apolloClientWithToken(XtraModule(), gqlClientId, gqlToken)
            .query(FollowedVideosQuery(id = Optional.Present(userId), sort = Optional.Present(gqlQuerySort), type = Optional.Present(typeList), first = Optional.Present(params.loadSize), after = Optional.Present(offset))).execute().data?.user?.followedVideos
        val get = get1?.edges
        val list = mutableListOf<Video>()
        if (get != null && nextPage && offset != null && offset != "") {
            for (i in get) {
                list.add(
                    Video(
                        id = i?.node?.id ?: "",
                        user_id = i?.node?.owner?.id,
                        user_login = i?.node?.owner?.login,
                        user_name = i?.node?.owner?.displayName,
                        gameId = i?.node?.game?.id,
                        gameName = i?.node?.game?.displayName,
                        type = i?.node?.broadcastType.toString(),
                        title = i?.node?.title,
                        view_count = i?.node?.viewCount,
                        createdAt = i?.node?.createdAt,
                        duration = i?.node?.lengthSeconds.toString(),
                        thumbnail_url = i?.node?.previewThumbnailURL,
                        profileImageURL = i?.node?.owner?.profileImageURL,
                    )
                )
            }
            offset = get.lastOrNull()?.cursor
            nextPage = get1.pageInfo?.hasNextPage ?: true
        }
        return list
    }

    private suspend fun gqlRange(params: LoadRangeParams): List<Video> {
        val get = gqlApi.loadFollowedVideos(gqlClientId, gqlToken, params.loadSize, offset)
        return if (!offset.isNullOrBlank()) {
            offset = get.cursor
            get.data
        } else mutableListOf()
    }

    class Factory(
        private val userId: String?,
        private val gqlClientId: String?,
        private val gqlToken: String?,
        private val gqlQueryType: BroadcastType?,
        private val gqlQuerySort: VideoSort?,
        private val gqlApi: GraphQLRepository,
        private val apiPref: ArrayList<Pair<Long?, String?>?>,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Video, FollowedVideosDataSource>() {

        override fun create(): DataSource<Int, Video> =
                FollowedVideosDataSource(userId, gqlClientId, gqlToken, gqlQueryType, gqlQuerySort, gqlApi, apiPref, coroutineScope).also(sourceLiveData::postValue)
    }
}
