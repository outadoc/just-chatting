package com.github.andreyasadchy.xtra.ui.saved.bookmarks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.model.offline.Bookmark
import com.github.andreyasadchy.xtra.model.offline.VodBookmarkIgnoredUser
import com.github.andreyasadchy.xtra.repository.BookmarksRepository
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.repository.VodBookmarkIgnoredUsersRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookmarksViewModel @Inject internal constructor(
    application: Application,
    private val repository: TwitchService,
    private val bookmarksRepository: BookmarksRepository,
    private val playerRepository: PlayerRepository,
    private val vodBookmarkIgnoredUsersRepository: VodBookmarkIgnoredUsersRepository) : AndroidViewModel(application) {

    val bookmarks = bookmarksRepository.loadBookmarks()
    val positions = playerRepository.loadVideoPositions()
    val ignoredUsers = vodBookmarkIgnoredUsersRepository.loadUsers()

    private val _user = MutableLiveData<List<User>>()
    val users: MutableLiveData<List<User>>
        get() = _user

    private val _video = MutableLiveData<List<Video>>()
    val videos: MutableLiveData<List<Video>>
        get() = _video

    fun delete(bookmark: Bookmark) {
        bookmarksRepository.deleteBookmark(bookmark)
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
        val allIds = bookmarks.value?.mapNotNull { bookmark -> bookmark.userId.takeUnless { it == null || ignoredUsers.value?.contains(VodBookmarkIgnoredUser(it)) == true } }
        if (!allIds.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    val list = mutableListOf<User>()
                    for (ids in allIds.chunked(100)) {
                        repository.loadUserTypes(ids, helixClientId, helixToken, gqlClientId)?.let { list.addAll(it) }
                    }
                    _user.postValue(list)
                } catch (e: Exception) {

                }
            }
        }
    }

    fun loadVideos(helixClientId: String? = null, helixToken: String? = null) {
        val allIds = bookmarks.value?.map { it.id }
        if (!allIds.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    val list = mutableListOf<Video>()
                    for (ids in allIds.chunked(100)) {
                        repository.loadVideos(ids, helixClientId, helixToken)?.let { list.addAll(it) }
                    }
                    _video.postValue(list)
                } catch (e: Exception) {

                }
            }
        }
    }
}