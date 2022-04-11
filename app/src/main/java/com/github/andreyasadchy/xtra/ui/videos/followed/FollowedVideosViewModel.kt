package com.github.andreyasadchy.xtra.ui.videos.followed

import android.app.Application
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
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.model.offline.Bookmark
import com.github.andreyasadchy.xtra.repository.BookmarksRepository
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.type.VideoSort
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosViewModel
import com.github.andreyasadchy.xtra.util.DownloadUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class FollowedVideosViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService,
        playerRepository: PlayerRepository,
        private val bookmarksRepository: BookmarksRepository) : BaseVideosViewModel(playerRepository, bookmarksRepository) {

    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Video>> = Transformations.map(filter) {
        repository.loadFollowedVideos(it.user.id, it.gqlClientId, it.user.gqlToken,
            when (it.broadcastType) {
                BroadcastType.ARCHIVE -> com.github.andreyasadchy.xtra.type.BroadcastType.ARCHIVE
                BroadcastType.HIGHLIGHT -> com.github.andreyasadchy.xtra.type.BroadcastType.HIGHLIGHT
                BroadcastType.UPLOAD -> com.github.andreyasadchy.xtra.type.BroadcastType.UPLOAD
                else -> null },
            when (it.sort) { Sort.TIME -> VideoSort.TIME else -> VideoSort.VIEWS }, it.apiPref, viewModelScope)
    }
    val sort: Sort
        get() = filter.value!!.sort
    val period: Period
        get() = filter.value!!.period
    val type: BroadcastType
        get() = filter.value!!.broadcastType

    init {
        _sortText.value = context.getString(R.string.sort_and_period, context.getString(R.string.upload_date), context.getString(R.string.all_time))
    }

    fun setUser(user: User, gqlClientId: String? = null, apiPref: ArrayList<Pair<Long?, String?>?>) {
        if (filter.value == null) {
            filter.value = Filter(user, gqlClientId, apiPref)
        }
    }

    fun filter(sort: Sort, period: Period, type: BroadcastType, text: CharSequence) {
        filter.value = filter.value?.copy(sort = sort, period = period, broadcastType = type)
        _sortText.value = text
    }

    private data class Filter(
        val user: User,
        val gqlClientId: String?,
        val apiPref: ArrayList<Pair<Long?, String?>?>,
        val sort: Sort = Sort.TIME,
        val period: Period = Period.ALL,
        val broadcastType: BroadcastType = BroadcastType.ALL)

    fun saveBookmark(context: Context, video: Video) {
        GlobalScope.launch {
            val item = bookmarksRepository.getBookmarkById(video.id)
            if (item != null) {
                bookmarksRepository.deleteBookmark(context, item)
            } else {
                try {
                    Glide.with(context)
                        .asBitmap()
                        .load(video.thumbnail)
                        .into(object: CustomTarget<Bitmap>() {
                            override fun onLoadCleared(placeholder: Drawable?) {

                            }

                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                DownloadUtils.savePng(context, "thumbnails", video.id, resource)
                            }
                        })
                } catch (e: Exception) {

                }
                try {
                    if (video.channelId != null) {
                        Glide.with(context)
                            .asBitmap()
                            .load(video.channelLogo)
                            .into(object: CustomTarget<Bitmap>() {
                                override fun onLoadCleared(placeholder: Drawable?) {

                                }

                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    DownloadUtils.savePng(context, "profile_pics", video.channelId!!, resource)
                                }
                            })
                    }
                } catch (e: Exception) {

                }
                val downloadedThumbnail = File(context.filesDir.toString() + File.separator + "thumbnails" + File.separator + "${video.id}.png").absolutePath
                val downloadedLogo = File(context.filesDir.toString() + File.separator + "profile_pics" + File.separator + "${video.channelId}.png").absolutePath
                bookmarksRepository.saveBookmark(
                    Bookmark(
                    id = video.id,
                    userId = video.channelId,
                    userLogin = video.channelLogin,
                    userName = video.channelName,
                    userLogo = downloadedLogo,
                    gameId = video.gameId,
                    gameName = video.gameName,
                    title = video.title,
                    createdAt = video.createdAt,
                    thumbnail = downloadedThumbnail,
                    type = video.type,
                    duration = video.duration,
                )
                )
            }
        }
    }
}
