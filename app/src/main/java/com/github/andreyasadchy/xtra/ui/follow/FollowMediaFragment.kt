package com.github.andreyasadchy.xtra.ui.follow

import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.MediaFragment
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsFragment
import com.github.andreyasadchy.xtra.ui.follow.games.FollowedGamesFragment
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsFragment

class FollowMediaFragment : MediaFragment() {

    override val spinnerItems: Array<String>
        get() = resources.getStringArray(R.array.spinnerFollowed)

    override fun onSpinnerItemSelected(position: Int): Fragment {
        return when (position) {
            0 -> FollowedStreamsFragment()
            1 -> FollowedChannelsFragment()
            else -> FollowedGamesFragment()
        }
    }
}