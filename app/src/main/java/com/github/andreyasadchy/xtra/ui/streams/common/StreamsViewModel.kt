package com.github.andreyasadchy.xtra.ui.streams.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.util.Pair
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
        if (it.gameId == null && it.gameName == null) {
            repository.loadTopStreams(it.helixClientId, it.helixToken, it.gqlClientId, it.tags, it.apiPref, it.thumbnailsEnabled, viewModelScope)
        } else {
            repository.loadGameStreams(it.gameId, it.gameName, it.helixClientId, it.helixToken, it.gqlClientId, it.tags, it.gameApiPref, it.thumbnailsEnabled, viewModelScope)
        }
    }

    fun loadStreams(gameId: String? = null, gameName: String? = null, helixClientId: String? = null, helixToken: String? = null, gqlClientId: String? = null, tags: List<String>? = null, apiPref: ArrayList<Pair<Long?, String?>?>, gameApiPref: ArrayList<Pair<Long?, String?>?>, thumbnailsEnabled: Boolean = true) {
        Filter(gameId, gameName, helixClientId, helixToken, gqlClientId, tags, apiPref, gameApiPref, thumbnailsEnabled).let {
            if (filter.value != it) {
                filter.value = it
            }
        }
    }

    private data class Filter(
        val gameId: String?,
        val gameName: String?,
        val helixClientId: String?,
        val helixToken: String?,
        val gqlClientId: String?,
        val tags: List<String>?,
        val apiPref: ArrayList<Pair<Long?, String?>?>,
        val gameApiPref: ArrayList<Pair<Long?, String?>?>,
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

    override fun setUser(user: User, helixClientId: String?, gqlClientId: String?, setting: Int) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(localFollowsGame = localFollowsGame, userId = userId, userLogin = userLogin, userName = userName, channelLogo = channelLogo, repository = repository, helixClientId = helixClientId, user = user, gqlClientId = gqlClientId, setting = setting, viewModelScope = viewModelScope)
        }
    }

    fun updateLocalGame(context: Context) {
        GlobalScope.launch {
            try {
                if (filter.value?.gameId != null) {
                    val get = repository.loadGameBoxArt(filter.value?.gameId!!, filter.value?.helixClientId, filter.value?.helixToken, filter.value?.gqlClientId)
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
