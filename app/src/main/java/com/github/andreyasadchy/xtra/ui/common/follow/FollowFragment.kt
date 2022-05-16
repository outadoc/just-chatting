package com.github.andreyasadchy.xtra.ui.common.follow

import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.util.FragmentUtils
import com.github.andreyasadchy.xtra.util.shortToast
import com.github.andreyasadchy.xtra.util.visible

interface FollowFragment {
    fun initializeFollow(fragment: Fragment, viewModel: FollowViewModel, followButton: ImageButton, setting: Int, user: User, helixClientId: String? = null, gqlClientId: String? = null) {
        val context = fragment.requireContext()
        with(viewModel) {
            setUser(user, helixClientId, gqlClientId, setting)
            followButton.visible()
            var initialized = false
            val channelName = userName
            follow.observe(fragment.viewLifecycleOwner) { following ->
                if (initialized) {
                    context.shortToast(context.getString(if (following) R.string.now_following else R.string.unfollowed, channelName))
                } else {
                    initialized = true
                }
                if (user.id != userId) {
                    followButton.setOnClickListener {
                        if (!following) {
                            if (game) {
                                follow.saveFollowGame(context)
                            } else {
                                follow.saveFollowChannel(context)
                            }
                            follow.value = true
                        } else {
                            if (channelName != null) {
                                FragmentUtils.showUnfollowDialog(context, channelName) {
                                    if (game) {
                                        follow.deleteFollowGame(context)
                                    } else {
                                        follow.deleteFollowChannel(context)
                                    }
                                    follow.value = false
                                }
                            }
                        }
                    }
                }
                followButton.setImageResource(if (following) R.drawable.baseline_favorite_black_24 else R.drawable.baseline_favorite_border_black_24)
            }
        }
    }
}