package com.github.andreyasadchy.xtra.ui.common.follow

import com.github.andreyasadchy.xtra.model.User

interface FollowViewModel {
    val userId: String?
    val userLogin: String?
    val userName: String?
    val channelLogo: String?
    val follow: FollowLiveData
    fun setUser(user: User, clientId: String?)
}