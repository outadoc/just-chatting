package com.github.andreyasadchy.xtra.ui.common.follow

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.MutableLiveData
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.offline.LocalFollowChannel
import com.github.andreyasadchy.xtra.repository.LocalFollowChannelRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.util.DownloadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.pathString

class FollowLiveData(
    private val localFollowsChannel: LocalFollowChannelRepository? = null,
    private val userId: String?,
    private val userLogin: String?,
    private val userName: String?,
    private var channelLogo: String?,
    private val repository: TwitchService,
    private val helixClientId: String? = null,
    private val user: User,
    private val viewModelScope: CoroutineScope
) : MutableLiveData<Boolean>() {

    init {
        viewModelScope.launch {
            try {
                val isFollowing = if (!user.helixToken.isNullOrBlank()) {
                    when {
                        localFollowsChannel != null
                                && !userId.isNullOrBlank()
                                && !user.id.isNullOrBlank()
                                && user.id != userId -> {
                            repository.loadUserFollowing(
                                helixClientId = helixClientId,
                                helixToken = user.helixToken,
                                userId = userId,
                                channelId = user.id,
                                userLogin = userLogin
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
                e.printStackTrace()
            }
        }
    }

    fun saveFollowChannel(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (userId == null) {
                    return@launch
                }

                try {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(channelLogo)
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

                localFollowsChannel?.saveFollow(
                    LocalFollowChannel(
                        user_id = userId,
                        user_login = userLogin,
                        user_name = userName,
                        channelLogo = downloadedLogoPath
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFollowChannel(context: Context) {
        viewModelScope.launch {
            try {
                if (userId != null) {
                    localFollowsChannel?.getFollowById(userId)
                        ?.let { localFollowsChannel.deleteFollow(context, it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
