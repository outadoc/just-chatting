package com.github.andreyasadchy.xtra.ui.common.follow

import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.util.FragmentUtils
import com.github.andreyasadchy.xtra.util.visible

interface FollowFragment { //TODO REFACTOR
    fun initializeFollow(fragment: Fragment, viewModel: FollowViewModel, followButton: ImageButton, user: LoggedIn) {
        val context = fragment.requireContext()
        with(viewModel) {
            setUser(user)
            followButton.visible()
            var initialized = false
            val channelName = channelId
            follow.observe(fragment.viewLifecycleOwner, Observer { following ->
                if (initialized) {

                } else {
                    initialized = true
                }
                followButton.setOnClickListener {
                    if (!following) {
                        follow.setValue(true)
                    } else {
                        FragmentUtils.showUnfollowDialog(context, channelName) { follow.setValue(false) }
                    }
                }
                followButton.setImageResource(if (following) R.drawable.baseline_favorite_black_24 else R.drawable.baseline_favorite_border_black_24)
            })
        }
    }
}