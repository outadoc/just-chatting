package com.github.andreyasadchy.xtra.ui.follow

import android.os.Bundle
import android.view.View
import com.github.andreyasadchy.xtra.ui.common.pagers.MediaPagerFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity

class FollowPagerFragment(private val defaultItem: Int?) : MediaPagerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        setAdapter(adapter = FollowPagerAdapter(activity, childFragmentManager),
            currentItem = when (defaultItem) {
                1 -> 2
                2 -> 0
                else -> 1
            })
    }

    override fun initialize() {
    }

    override fun onNetworkRestored() {
    }
}