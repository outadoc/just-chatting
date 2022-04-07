package com.github.andreyasadchy.xtra.repository.datasource

import androidx.paging.DataSource
import com.apollographql.apollo3.api.Optional
import com.github.andreyasadchy.xtra.UserTypeQuery
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.di.XtraModule
import com.github.andreyasadchy.xtra.di.XtraModule_ApolloClientFactory.apolloClient
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.repository.OfflineRepository
import com.github.andreyasadchy.xtra.repository.VodBookmarkIgnoredUsersRepository
import kotlinx.coroutines.CoroutineScope

class OfflineVideosDataSource private constructor(
    private val offlineRepository: OfflineRepository,
    private val vodBookmarkIgnoredUsersRepository: VodBookmarkIgnoredUsersRepository,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixApi: HelixApi,
    private val gqlClientId: String?,
    private val vodTimeLeft: Boolean?,
    private val currentList: List<OfflineVideo>?,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<OfflineVideo>(coroutineScope) {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<OfflineVideo>) {
        loadInitial(params, callback) {
            val list = offlineRepository.loadAllVideos()
            val ignore = vodBookmarkIgnoredUsersRepository.loadUsers().map { it.user_id }
            val bookmarks = list.filter { it.bookmark == true && it.type?.lowercase() == "archive" }
            val current = currentList?.filter { it.bookmark == true && it.type?.lowercase() == "archive" }
            val noNewBookmarks = current?.containsAll(bookmarks) ?: false
            val allIds = bookmarks.mapNotNull { video -> video.channelId.takeUnless { ignore.contains(it) } }
            if (vodTimeLeft == true && !noNewBookmarks && allIds.isNotEmpty()) {
                try {
                    for (ids in allIds.chunked(100)) {
                        val get = apolloClient(XtraModule(), gqlClientId).query(UserTypeQuery(Optional.Present(ids))).execute().data?.users
                        if (get != null) {
                            for (user in get) {
                                val broadcasterType = when {
                                    user?.roles?.isPartner == true -> "partner"
                                    user?.roles?.isAffiliate == true -> "affiliate"
                                    else -> null
                                }
                                val type = when {
                                    user?.roles?.isStaff == true -> "staff"
                                    user?.roles?.isSiteAdmin == true -> "admin"
                                    user?.roles?.isGlobalMod == true -> "global_mod"
                                    else -> null
                                }
                                val videos = list.filter { it.channelId == user?.id && it.bookmark == true && it.type?.lowercase() == "archive" }
                                for (i in videos) {
                                    i.userType = type ?: broadcasterType ?: ""
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    try {
                        if (!helixToken.isNullOrEmpty()) {
                            for (ids in allIds.chunked(100)) {
                                val get = helixApi.getUserById(helixClientId, helixToken, ids).data
                                if (get != null) {
                                    for (user in get) {
                                        val videos = list.filter { it.channelId == user.id && it.bookmark == true && it.type?.lowercase() == "archive" }
                                        for (i in videos) {
                                            i.userType = if (!user.type.isNullOrBlank()) user.type else user.broadcaster_type ?: ""
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {

                    }
                }
            }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<OfflineVideo>) {
        loadRange(params, callback) {
            mutableListOf()
        }
    }

    class Factory(
        private val offlineRepository: OfflineRepository,
        private val vodBookmarkIgnoredUsersRepository: VodBookmarkIgnoredUsersRepository,
        private val helixClientId: String?,
        private val helixToken: String?,
        private val helixApi: HelixApi,
        private val gqlClientId: String?,
        private val vodTimeLeft: Boolean?,
        private val currentList: List<OfflineVideo>?,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, OfflineVideo, OfflineVideosDataSource>() {

        override fun create(): DataSource<Int, OfflineVideo> =
                OfflineVideosDataSource(offlineRepository, vodBookmarkIgnoredUsersRepository, helixClientId, helixToken, helixApi, gqlClientId, vodTimeLeft, currentList, coroutineScope).also(sourceLiveData::postValue)
    }
}
