package com.github.andreyasadchy.xtra.ui.follow

import android.os.Bundle
import android.view.View
import com.github.andreyasadchy.xtra.ui.common.pagers.MediaPagerFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity

class FollowPagerFragment : MediaPagerFragment() {

    companion object {
        private const val DEFAULT_ITEM = "default_item"
        private const val LOGGED_IN = "logged_in"

        fun newInstance(defaultItem: Int?, loggedIn: Boolean) = FollowPagerFragment().apply {
            arguments = Bundle().apply {
                putInt(DEFAULT_ITEM, defaultItem ?: 0)
                putBoolean(LOGGED_IN, loggedIn)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        setAdapter(
            adapter = FollowPagerAdapter(activity, childFragmentManager),
            currentItem = 0
        )
    }

    override fun initialize() {
    }

    override fun onNetworkRestored() {
    }
}