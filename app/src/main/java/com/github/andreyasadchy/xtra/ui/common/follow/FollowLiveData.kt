package com.github.andreyasadchy.xtra.ui.common.follow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.offline.LocalFollow
import com.github.andreyasadchy.xtra.repository.LocalFollowRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.util.DownloadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

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
                    try {
                        Glide.with(context)
                            .asBitmap()
                            .load(channelLogo)
                            .into(object: CustomTarget<Bitmap>() {
                                override fun onLoadCleared(placeholder: Drawable?) {

                                }

                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    DownloadUtils.savePng(context, "profile_pics", userId, resource)
                                }
                            })
                    } catch (e: Exception) {

                    }
                    val downloadedLogo = File(context.filesDir.toString() + File.separator + "profile_pics" + File.separator + "${userId}.png").absolutePath
                    localFollows.saveFollow(LocalFollow(userId, userLogin, userName, downloadedLogo))
                }
            } catch (e: Exception) {

            }
        }
    }

    fun deleteFollow(context: Context) {
        viewModelScope.launch {
            try {
                if (userId != null) {
                    localFollows.getFollowById(userId)?.let { localFollows.deleteFollow(context, it) }
                }
            } catch (e: Exception) {

            }
        }
    }
}