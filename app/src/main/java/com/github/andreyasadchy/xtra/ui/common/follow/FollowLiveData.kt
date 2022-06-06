package com.github.andreyasadchy.xtra.ui.common.follow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.offline.LocalFollowChannel
import com.github.andreyasadchy.xtra.repository.LocalFollowChannelRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.util.DownloadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class FollowLiveData(
    private val localFollowsChannel: LocalFollowChannelRepository? = null,
    private val userId: String?,
    private val userLogin: String?,
    private val userName: String?,
    private var channelLogo: String?,
    private val repository: TwitchService,
    private val helixClientId: String? = null,
    private val user: User,
    private val gqlClientId: String? = null,
    private val viewModelScope: CoroutineScope
) : MutableLiveData<Boolean>() {

    init {
        viewModelScope.launch {
            try {
                val isFollowing = if (!user.gqlToken.isNullOrBlank()) {
                    when {
                        localFollowsChannel != null && (
                            (!user.helixToken.isNullOrBlank() && !userId.isNullOrBlank() && !user.id.isNullOrBlank()) ||
                                (!user.gqlToken.isNullOrBlank() && !userLogin.isNullOrBlank())
                            ) && user.id != userId -> {
                            repository.loadUserFollowing(
                                helixClientId,
                                user.helixToken,
                                userId,
                                user.id,
                                gqlClientId,
                                user.gqlToken,
                                userLogin
                            )
                        }
                        else -> false
                    }
                } else {
                    userId?.let {
                        localFollowsChannel?.getFollowById(it)
                    } != null
                }
                super.setValue(isFollowing)
            } catch (e: Exception) {
            }
        }
    }

    fun saveFollowChannel(context: Context) {
        GlobalScope.launch {
            try {
                if (!user.gqlToken.isNullOrBlank()) {
                    repository.followUser(gqlClientId, user.gqlToken, userId)
                } else {
                    if (userId != null) {
                        try {
                            Glide.with(context)
                                .asBitmap()
                                .load(channelLogo)
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onLoadCleared(placeholder: Drawable?) {
                                    }

                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        DownloadUtils.savePng(
                                            context,
                                            "profile_pics",
                                            userId,
                                            resource
                                        )
                                    }
                                })
                        } catch (e: Exception) {
                        }
                        val downloadedLogo =
                            File(context.filesDir.toString() + File.separator + "profile_pics" + File.separator + "$userId.png").absolutePath
                        localFollowsChannel?.saveFollow(
                            LocalFollowChannel(
                                userId,
                                userLogin,
                                userName,
                                downloadedLogo
                            )
                        )
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    fun deleteFollowChannel(context: Context) {
        viewModelScope.launch {
            try {
                if (!user.gqlToken.isNullOrBlank()) {
                    repository.unfollowUser(gqlClientId, user.gqlToken, userId)
                } else {
                    if (userId != null) {
                        localFollowsChannel?.getFollowById(userId)
                            ?.let { localFollowsChannel.deleteFollow(context, it) }
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}
