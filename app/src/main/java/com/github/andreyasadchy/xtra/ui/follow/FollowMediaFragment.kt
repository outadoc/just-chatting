package com.github.andreyasadchy.xtra.ui.follow

import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.MediaFragment
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsFragment
import com.github.andreyasadchy.xtra.ui.follow.games.FollowedGamesFragment
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsFragment
import kotlinx.android.synthetic.main.fragment_media.*

class FollowMediaFragment(
    private val followPager: Boolean,
    private val defaultItem: Int?) : MediaFragment() {

    private var firstLaunch = true

    override val spinnerItems: Array<String>
        get() = resources.getStringArray(R.array.spinnerFollowed)

    override fun onSpinnerItemSelected(position: Int): Fragment {
        if (followPager) {
            hideSpinner = true
            return FollowPagerFragment(defaultItem)
        }
        if (firstLaunch && defaultItem != null) {
            spinner.setSelection(defaultItem)
            firstLaunch = false
        }
        return when (position) {
            0 -> FollowedStreamsFragment()
            1 -> FollowedChannelsFragment()
            else -> FollowedGamesFragment()
        }
    }
}