package com.github.andreyasadchy.xtra.ui.videos.game

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
import com.github.andreyasadchy.xtra.model.offline.SortGame
import com.github.andreyasadchy.xtra.repository.*
import com.github.andreyasadchy.xtra.type.VideoSort
import com.github.andreyasadchy.xtra.ui.common.follow.FollowLiveData
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosViewModel
import com.github.andreyasadchy.xtra.util.DownloadUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

class GameVideosViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService,
        playerRepository: PlayerRepository,
        private val localFollowsGame: LocalFollowGameRepository,
        private val bookmarksRepository: BookmarksRepository,
        private val sortGameRepository: SortGameRepository) : BaseVideosViewModel(playerRepository, bookmarksRepository), FollowViewModel {

    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Video>> = Transformations.map(filter) {
        val langValues = context.resources.getStringArray(R.array.gqlUserLanguageValues).toList()
        val language = if (languageIndex != 0) {
            langValues.elementAt(languageIndex)
        } else null
        repository.loadGameVideos(it.gameId, it.gameName, it.helixClientId, it.helixToken, it.period, it.broadcastType, language?.lowercase(), it.sort, it.gqlClientId,
            if (language != null) {
                val langList = mutableListOf<String>()
                langList.add(language)
                langList
            } else null,
            when (it.broadcastType) {
                BroadcastType.ARCHIVE -> com.github.andreyasadchy.xtra.type.BroadcastType.ARCHIVE
                BroadcastType.HIGHLIGHT -> com.github.andreyasadchy.xtra.type.BroadcastType.HIGHLIGHT
                BroadcastType.UPLOAD -> com.github.andreyasadchy.xtra.type.BroadcastType.UPLOAD
                else -> null },
            when (it.sort) { Sort.TIME -> VideoSort.TIME else -> VideoSort.VIEWS },
            if (it.broadcastType == BroadcastType.ALL) { null }
            else { it.broadcastType.value.uppercase() },
            it.sort.value.uppercase(), it.apiPref, viewModelScope)
    }
    val sort: Sort
        get() = filter.value!!.sort
    val period: Period
        get() = filter.value!!.period
    val type: BroadcastType
        get() = filter.value!!.broadcastType
    val languageIndex: Int
        get() = filter.value!!.languageIndex
    val saveSort: Boolean
        get() = filter.value?.saveSort == true

    fun setGame(context: Context, gameId: String? = null, gameName: String? = null, helixClientId: String? = null, helixToken: String? = null, gqlClientId: String? = null, apiPref: ArrayList<Pair<Long?, String?>?>) {
        if (filter.value?.gameId != gameId || filter.value?.gameName != gameName) {
            var sortValues = gameId?.let { runBlocking { sortGameRepository.getById(it) } }
            if (sortValues?.saveSort != true) {
                sortValues = runBlocking { sortGameRepository.getById("default") }
            }
            filter.value = Filter(
                gameId = gameId,
                gameName = gameName,
                helixClientId = helixClientId,
                helixToken = helixToken,
                gqlClientId = gqlClientId,
                apiPref = apiPref,
                saveSort = sortValues?.saveSort,
                sort = when (sortValues?.videoSort) {
                    Sort.TIME.value -> Sort.TIME
                    else -> Sort.VIEWS
                },
                period = if (helixToken.isNullOrBlank()) {
                    Period.WEEK
                } else {
                    when (sortValues?.videoPeriod) {
                        Period.DAY.value -> Period.DAY
                        Period.MONTH.value -> Period.MONTH
                        Period.ALL.value -> Period.ALL
                        else -> Period.WEEK
                    }
                },
                broadcastType = when (sortValues?.videoType) {
                    BroadcastType.ARCHIVE.value -> BroadcastType.ARCHIVE
                    BroadcastType.HIGHLIGHT.value -> BroadcastType.HIGHLIGHT
                    BroadcastType.UPLOAD.value -> BroadcastType.UPLOAD
                    else -> BroadcastType.ALL
                },
                languageIndex = sortValues?.videoLanguageIndex ?: 0
            )
            _sortText.value = context.getString(R.string.sort_and_period,
                when (sortValues?.videoSort) {
                    Sort.TIME.value -> context.getString(R.string.upload_date)
                    else -> context.getString(R.string.view_count)
                },
                when (sortValues?.videoPeriod) {
                    Period.DAY.value -> context.getString(R.string.today)
                    Period.MONTH.value -> context.getString(R.string.this_month)
                    Period.ALL.value -> context.getString(R.string.all_time)
                    else -> context.getString(R.string.this_week)
                }
            )
        }
    }

    fun filter(sort: Sort, period: Period, type: BroadcastType, languageIndex: Int, text: CharSequence, saveSort: Boolean) {
        filter.value = filter.value?.copy(saveSort = saveSort, sort = sort, period = period, broadcastType = type, languageIndex = languageIndex)
        _sortText.value = text
        viewModelScope.launch {
            val sortValues = filter.value?.gameId?.let { sortGameRepository.getById(it) }
            if (saveSort) {
                (sortValues?.apply {
                    this.saveSort = saveSort
                    videoSort = sort.value
                    if (!filter.value?.helixToken.isNullOrBlank()) videoPeriod = period.value
                    videoType = type.value
                    videoLanguageIndex = languageIndex
                } ?: filter.value?.gameId?.let { SortGame(
                    id = it,
                    saveSort = saveSort,
                    videoSort = sort.value,
                    videoPeriod = if (filter.value?.helixToken.isNullOrBlank()) null else period.value,
                    videoType = type.value,
                    videoLanguageIndex = languageIndex)
                })?.let { sortGameRepository.save(it) }
            } else {
                (sortValues?.apply {
                    this.saveSort = saveSort
                } ?: filter.value?.gameId?.let { SortGame(
                    id = it,
                    saveSort = saveSort)
                })?.let { sortGameRepository.save(it) }
                val sortDefaults = sortGameRepository.getById("default")
                (sortDefaults?.apply {
                    videoSort = sort.value
                    if (!filter.value?.helixToken.isNullOrBlank()) videoPeriod = period.value
                    videoType = type.value
                    videoLanguageIndex = languageIndex
                } ?: SortGame(
                    id = "default",
                    videoSort = sort.value,
                    videoPeriod = if (filter.value?.helixToken.isNullOrBlank()) null else period.value,
                    videoType = type.value,
                    videoLanguageIndex = languageIndex
                )).let { sortGameRepository.save(it) }
            }
        }
    }

    private data class Filter(
        val gameId: String?,
        val gameName: String?,
        val helixClientId: String?,
        val helixToken: String?,
        val gqlClientId: String?,
        val apiPref: ArrayList<Pair<Long?, String?>?>,
        val saveSort: Boolean?,
        val sort: Sort = Sort.VIEWS,
        val period: Period = Period.WEEK,
        val broadcastType: BroadcastType = BroadcastType.ALL,
        val languageIndex: Int = 0)

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
                val userTypes = video.channelId?.let { repository.loadUserTypes(mutableListOf(it), filter.value?.helixClientId, filter.value?.helixToken, filter.value?.gqlClientId) }?.first()
                val downloadedThumbnail = File(context.filesDir.toString() + File.separator + "thumbnails" + File.separator + "${video.id}.png").absolutePath
                val downloadedLogo = File(context.filesDir.toString() + File.separator + "profile_pics" + File.separator + "${video.channelId}.png").absolutePath
                bookmarksRepository.saveBookmark(
                    Bookmark(
                    id = video.id,
                    userId = video.channelId,
                    userLogin = video.channelLogin,
                    userName = video.channelName,
                    userType = userTypes?.type,
                    userBroadcasterType = userTypes?.broadcaster_type,
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
