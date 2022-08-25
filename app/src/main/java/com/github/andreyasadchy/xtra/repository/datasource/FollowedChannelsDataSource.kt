package com.github.andreyasadchy.xtra.repository.datasource

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.paging.DataSource
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.github.andreyasadchy.xtra.MainApplication
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.follows.Sort
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.repository.LocalFollowChannelRepository
import com.github.andreyasadchy.xtra.util.DownloadUtils
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.pathString

class FollowedChannelsDataSource(
    private val localFollowsChannel: LocalFollowChannelRepository,
    private val userId: String?,
    private val helixClientId: String?,
    private val helixToken: String?,
    private val helixApi: HelixApi,
    private val sort: Sort,
    private val order: Order,
    private val coroutineScope: CoroutineScope
) : BasePositionalDataSource<Follow>(coroutineScope) {

    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Follow>) {
        if (helixToken.isNullOrBlank()) return

        loadInitial(params, callback) {
            val localFollows: Map<String?, Follow> =
                localFollowsChannel
                    .loadFollows()
                    .map { follow ->
                        Follow(
                            to_id = follow.user_id,
                            to_login = follow.user_login,
                            to_name = follow.user_name,
                            profileImageURL = follow.channelLogo,
                            followLocal = true
                        )
                    }
                    .associateBy { it.to_id }

            val helixFollows: Map<String?, Follow> =
                helixApi.getFollowedChannels(
                    clientId = helixClientId,
                    token = helixToken,
                    userId = userId,
                    limit = 100,
                    offset = offset
                )
                    .also { offset = it.pagination?.cursor }
                    .data
                    .orEmpty()
                    .map { follow ->
                        val localFollow = localFollows[follow.to_id]
                        localFollow?.copy(
                            followTwitch = true,
                            followed_at = follow.followed_at,
                            lastBroadcast = follow.lastBroadcast
                        ) ?: follow.copy(
                            followTwitch = true
                        )
                    }
                    .associateBy { it.to_id }

            val list: Collection<Follow> =
                localFollows
                    .plus(helixFollows)
                    .values
                    .mapWithUserProfileImages()
                    .also { follows ->
                        follows.filter { follow -> follow.followLocal }
                            .forEach(::updateLocalProfileImage)
                    }

            when (order) {
                Order.ASC -> when (sort) {
                    Sort.FOLLOWED_AT -> list.sortedBy { it.followed_at }
                    else -> list.sortedBy { it.to_login }
                }
                Order.DESC -> when (sort) {
                    Sort.FOLLOWED_AT -> list.sortedByDescending { it.followed_at }
                    else -> list.sortedByDescending { it.to_login }
                }
            }
        }
    }

    private fun updateLocalProfileImage(follow: Follow) {
        val id = follow.to_id ?: return
        val profileImageUrl = follow.profileImageURL ?: return
        updateLocalUser(
            context = MainApplication.INSTANCE.applicationContext,
            userId = id,
            profileImageURL = profileImageUrl
        )
    }

    private suspend fun Collection<Follow>.mapWithUserProfileImages(): Collection<Follow> {
        val results: List<User> =
            filter { follow ->
                follow.profileImageURL == null ||
                    follow.profileImageURL.contains("image_manager_disk_cache") ||
                    follow.lastBroadcast == null
            }
                .mapNotNull { follow -> follow.to_id }
                .chunked(size = 100)
                .flatMap { idsToUpdate ->
                    helixApi.getUsersById(
                        clientId = helixClientId,
                        token = helixToken,
                        ids = idsToUpdate
                    )
                        .data
                        .orEmpty()
                }

        return map { follow ->
            val userInfo = results.firstOrNull { user -> user.id == follow.to_id }
            follow.copy(
                profileImageURL = userInfo?.profile_image_url
            )
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Follow>) {
        check(!helixToken.isNullOrBlank())
        if (offset.isNullOrBlank()) return

        loadRange(params, callback) {
            helixApi.getFollowedChannels(
                clientId = helixClientId,
                token = helixToken,
                userId = userId,
                limit = 100,
                offset = offset
            )
                .also { offset = it.pagination?.cursor }
                .data
                .orEmpty()
                .mapWithUserProfileImages()
                .toList()
                .also { follows ->
                    follows.filter { follow -> follow.followLocal }
                        .forEach(::updateLocalProfileImage)
                }
        }
    }

    private fun updateLocalUser(context: Context, userId: String, profileImageURL: String) {
        coroutineScope.launch {
            try {
                try {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(TwitchApiHelper.getTemplateUrl(profileImageURL, "profileimage"))
                        .build()

                    val result = (loader.execute(request) as SuccessResult).drawable
                    val bitmap = (result as BitmapDrawable).bitmap

                    DownloadUtils.savePng(
                        context = context,
                        folder = "profile_pics",
                        fileName = userId,
                        bitmap = bitmap
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val downloadedLogoPath: String =
                    Path(context.filesDir.path, "profile_pics", "$userId.png")
                        .absolute()
                        .pathString

                localFollowsChannel.getFollowById(userId)?.let { follow ->
                    localFollowsChannel.updateFollow(
                        follow.apply {
                            channelLogo = downloadedLogoPath
                        }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    class Factory(
        private val localFollowsChannel: LocalFollowChannelRepository,
        private val userId: String?,
        private val helixClientId: String?,
        private val helixToken: String?,
        private val helixApi: HelixApi,
        private val sort: Sort,
        private val order: Order,
        private val coroutineScope: CoroutineScope
    ) : BaseDataSourceFactory<Int, Follow, FollowedChannelsDataSource>() {

        override fun create(): DataSource<Int, Follow> =
            FollowedChannelsDataSource(
                localFollowsChannel = localFollowsChannel,
                userId = userId,
                helixClientId = helixClientId,
                helixToken = helixToken,
                helixApi = helixApi,
                sort = sort,
                order = order,
                coroutineScope = coroutineScope
            ).also(sourceLiveData::postValue)
    }
}
