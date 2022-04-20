package com.github.andreyasadchy.xtra.ui.saved.bookmarks

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.model.offline.Bookmark
import com.github.andreyasadchy.xtra.model.offline.VodBookmarkIgnoredUser
import com.github.andreyasadchy.xtra.repository.BookmarksRepository
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.repository.VodBookmarkIgnoredUsersRepository
import com.github.andreyasadchy.xtra.util.DownloadUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class BookmarksViewModel @Inject internal constructor(
    application: Application,
    private val repository: TwitchService,
    private val bookmarksRepository: BookmarksRepository,
    private val playerRepository: PlayerRepository,
    private val vodBookmarkIgnoredUsersRepository: VodBookmarkIgnoredUsersRepository) : AndroidViewModel(application) {

    val bookmarks = bookmarksRepository.loadBookmarksLiveData()
    val positions = playerRepository.loadVideoPositions()
    val ignoredUsers = vodBookmarkIgnoredUsersRepository.loadUsers()

    fun delete(context: Context, bookmark: Bookmark) {
        bookmarksRepository.deleteBookmark(context, bookmark)
    }

    fun vodIgnoreUser(userId: String) {
        GlobalScope.launch {
            if (vodBookmarkIgnoredUsersRepository.getUserById(userId) != null) {
                vodBookmarkIgnoredUsersRepository.deleteUser(VodBookmarkIgnoredUser(userId))
            } else {
                vodBookmarkIgnoredUsersRepository.saveUser(VodBookmarkIgnoredUser(userId))
            }
        }
    }

    fun loadUsers(helixClientId: String? = null, helixToken: String? = null, gqlClientId: String? = null) {
        viewModelScope.launch {
            try {
                val allIds = bookmarksRepository.loadBookmarks().mapNotNull { bookmark -> bookmark.userId.takeUnless { it == null || ignoredUsers.value?.contains(VodBookmarkIgnoredUser(it)) == true } }
                if (!allIds.isNullOrEmpty()) {
                    for (ids in allIds.chunked(100)) {
                        val users = repository.loadUserTypes(ids, helixClientId, helixToken, gqlClientId)
                        if (users != null) {
                            for (user in users) {
                                val bookmarks = user.id?.let { bookmarksRepository.getBookmarksByUserId(it) }
                                if (bookmarks != null) {
                                    for (bookmark in bookmarks) {
                                        if (user.type != bookmark.userType || user.broadcaster_type != bookmark.userBroadcasterType) {
                                            bookmarksRepository.updateBookmark(bookmark.apply {
                                                userType = user.type
                                                userBroadcasterType = user.broadcaster_type
                                            })
                                        }
                                    }
                                }
                            }
                        }
                    }}
            } catch (e: Exception) {

            }
        }
    }

    fun loadVideo(context: Context, helixClientId: String? = null, helixToken: String? = null, gqlClientId: String? = null, videoId: String? = null) {
        viewModelScope.launch {
            try {
                val video = videoId?.let { repository.loadVideo(it, helixClientId, helixToken, gqlClientId) }
                val bookmark = videoId?.let { bookmarksRepository.getBookmarkById(it) }
                if (video != null && bookmark != null) {
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
                    val downloadedThumbnail = File(context.filesDir.toString() + File.separator + "thumbnails" + File.separator + "${video.id}.png").absolutePath
                    bookmarksRepository.updateBookmark(Bookmark(
                        id = bookmark.id,
                        userId = video.channelId ?: bookmark.userId,
                        userLogin = video.channelLogin ?: bookmark.userLogin,
                        userName = video.channelName ?: bookmark.userName,
                        userType = bookmark.userType,
                        userBroadcasterType = bookmark.userBroadcasterType,
                        userLogo = bookmark.userLogo,
                        gameId = video.gameId ?: bookmark.gameId,
                        gameName = video.gameName ?: bookmark.gameName,
                        title = video.title ?: bookmark.title,
                        createdAt = video.createdAt ?: bookmark.createdAt,
                        thumbnail = downloadedThumbnail,
                        type = video.type ?: bookmark.type,
                        duration = video.duration ?: bookmark.duration,
                    ))
                }
            } catch (e: Exception) {

            }
        }
    }

    fun loadVideos(context: Context, helixClientId: String? = null, helixToken: String? = null) {
        viewModelScope.launch {
            try {
                val allIds = bookmarksRepository.loadBookmarks().map { it.id }
                if (!allIds.isNullOrEmpty()) {
                    for (ids in allIds.chunked(100)) {
                        val videos = repository.loadVideos(ids, helixClientId, helixToken)
                        if (videos != null) {
                            for (video in videos) {
                                val bookmark = bookmarksRepository.getBookmarkById(video.id)
                                if (bookmark != null && (bookmark.userId != video.channelId ||
                                            bookmark.userLogin != video.channelLogin ||
                                            bookmark.userName != video.channelName ||
                                            bookmark.title != video.title ||
                                            bookmark.createdAt != video.createdAt ||
                                            bookmark.type != video.type ||
                                            bookmark.duration != video.duration)) {
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
                                    val downloadedThumbnail = File(context.filesDir.toString() + File.separator + "thumbnails" + File.separator + "${video.id}.png").absolutePath
                                    bookmarksRepository.updateBookmark(Bookmark(
                                        id = bookmark.id,
                                        userId = video.channelId ?: bookmark.userId,
                                        userLogin = video.channelLogin ?: bookmark.userLogin,
                                        userName = video.channelName ?: bookmark.userName,
                                        userType = bookmark.userType,
                                        userBroadcasterType = bookmark.userBroadcasterType,
                                        userLogo = bookmark.userLogo,
                                        gameId = bookmark.gameId,
                                        gameName = bookmark.gameName,
                                        title = video.title ?: bookmark.title,
                                        createdAt = video.createdAt ?: bookmark.createdAt,
                                        thumbnail = downloadedThumbnail,
                                        type = video.type ?: bookmark.type,
                                        duration = video.duration ?: bookmark.duration,
                                    ))
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }
    }
}