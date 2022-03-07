package com.github.andreyasadchy.xtra.ui.streams.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.LocalFollowGameRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import com.github.andreyasadchy.xtra.ui.common.follow.FollowLiveData
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import com.github.andreyasadchy.xtra.util.DownloadUtils
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class StreamsViewModel @Inject constructor(
        private val repository: TwitchService,
        private val localFollowsGame: LocalFollowGameRepository) : PagedListViewModel<Stream>(), FollowViewModel {

    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Stream>> = Transformations.map(filter) {
        if (it.useHelix) {
            repository.loadTopStreams(it.clientId, it.token, it.gameId, it.thumbnailsEnabled, viewModelScope)
        } else {
            if (it.tags == null && !it.showTags) {
                if (it.gameId == null) {
                    repository.loadTopStreamsGQLQuery(it.clientId, it.thumbnailsEnabled, viewModelScope)
                } else {
                    repository.loadGameStreamsGQLQuery(it.clientId, it.gameId, null, viewModelScope)
                }
            } else {
                if (it.gameName == null) {
                    repository.loadTopStreamsGQL(it.clientId, it.tags, it.thumbnailsEnabled, viewModelScope)
                } else {
                    repository.loadGameStreamsGQL(it.clientId, it.gameName, it.tags, viewModelScope)
                }
            }
        }
    }

    fun loadStreams(useHelix: Boolean, showTags: Boolean, clientId: String?, token: String? = null, channelId: String? = null, gameId: String? = null, gameName: String? = null, tags: List<String>? = null, languages: String? = null, thumbnailsEnabled: Boolean = true) {
        Filter(useHelix, showTags, clientId, token, channelId, gameId, gameName, tags, languages, thumbnailsEnabled).let {
            if (filter.value != it) {
                filter.value = it
            }
        }
    }

    private data class Filter(
            val useHelix: Boolean,
            val showTags: Boolean,
            val clientId: String?,
            val token: String?,
            val channelId: String?,
            val gameId: String?,
            val gameName: String?,
            val tags: List<String>?,
            val languages: String?,
            val thumbnailsEnabled: Boolean)

    override val userId: String?
        get() { return filter.value?.gameId }
    override val userLogin: String?
        get() = null
    override val userName: String?
        get() { return filter.value?.gameName }
    override val channelLogo: String?
        get() = null
    override val game: Boolean
        get() = true
    override lateinit var follow: FollowLiveData

    override fun setUser(user: User, helixClientId: String?, gqlClientId: String?) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(localFollowsGame = localFollowsGame, userId = userId, userLogin = userLogin, userName = userName, channelLogo = channelLogo, repository = repository, helixClientId = helixClientId, user = user, gqlClientId = gqlClientId, viewModelScope = viewModelScope)
        }
    }

    fun updateLocalGame(context: Context) {
        GlobalScope.launch {
            try {
                if (filter.value?.gameId != null) {
                    val get = if (filter.value?.useHelix == true) {
                        repository.loadGame(filter.value?.clientId, filter.value?.token, filter.value?.gameId!!)?.boxArt
                    } else {
                        repository.loadGameBoxArtGQLQuery(filter.value?.clientId, filter.value?.gameId!!)
                    }
                    try {
                        Glide.with(context)
                            .asBitmap()
                            .load(TwitchApiHelper.getTemplateUrl(get, "game"))
                            .into(object: CustomTarget<Bitmap>() {
                                override fun onLoadCleared(placeholder: Drawable?) {

                                }

                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    DownloadUtils.savePng(context, "box_art", filter.value?.gameId!!, resource)
                                }
                            })
                    } catch (e: Exception) {

                    }
                    val downloadedLogo = File(context.filesDir.toString() + File.separator + "box_art" + File.separator + "${filter.value?.gameId}.png").absolutePath
                    localFollowsGame.getFollowById(filter.value?.gameId!!)?.let { localFollowsGame.updateFollow(it.apply {
                        game_name = filter.value?.gameName
                        boxArt = downloadedLogo }) }
                }
            } catch (e: Exception) {

            }
        }
    }
}
