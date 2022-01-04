package com.github.andreyasadchy.xtra.ui.common.follow

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.github.andreyasadchy.xtra.GlideApp
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.offline.LocalFollow
import com.github.andreyasadchy.xtra.repository.LocalFollowRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FollowLiveData(
    private val localFollows: LocalFollowRepository,
    private val userId: String?,
    private val userLogin: String?,
    private val userName: String?,
    private val channelLogo: String?,
    private val repository: TwitchService,
    private val clientId: String?,
    private val user: User,
    private val viewModelScope: CoroutineScope) : MutableLiveData<Boolean>()  {

    init {
        viewModelScope.launch {
            try {
                // val isFollowing = userId?.let { repository.loadUserFollows(clientId, user.token, it, user.id) }
                val isLocalFollowing = userId?.let { localFollows.getFollowById(it) } != null
                super.setValue(isLocalFollowing)
            } catch (e: Exception) {

            }
        }
    }

    fun saveFollow(context: Context) {
        GlobalScope.launch {
            try {
                if (userId != null) {
                    val glide = GlideApp.with(context)
                    val downloadedLogo: String? = try {
                        glide.downloadOnly().load(channelLogo).submit().get().absolutePath
                    } catch (e: Exception) {
                        channelLogo
                    }
                    localFollows.saveFollow(LocalFollow(userId, userLogin, userName, downloadedLogo))
                }
            } catch (e: Exception) {

            }
        }
    }

    fun deleteFollow() {
        viewModelScope.launch {
            try {
                if (userId != null) {
                    localFollows.getFollowById(userId)?.let { localFollows.deleteFollow(it) }
                }
            } catch (e: Exception) {

            }
        }
    }
}