package com.github.andreyasadchy.xtra.ui.follow

import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.MediaFragment
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsFragment
import com.github.andreyasadchy.xtra.ui.follow.games.FollowedGamesFragment
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsFragment
import com.github.andreyasadchy.xtra.ui.videos.followed.FollowedVideosFragment
import kotlinx.android.synthetic.main.fragment_media.*

class FollowMediaFragment(
    private val followPager: Boolean,
    private val defaultItem: Int?,
    private val loggedIn: Boolean) : MediaFragment() {

    private var firstLaunch = true

    override val spinnerItems: Array<String>
        get() = resources.getStringArray(if (loggedIn) R.array.spinnerFollowedEntries else R.array.spinnerFollowedEntriesNotLoggedIn)

    override fun onSpinnerItemSelected(position: Int): Fragment {
        if (followPager) {
            hideSpinner = true
            return FollowPagerFragment(defaultItem, loggedIn)
        }
        if (firstLaunch && defaultItem != null) {
            spinner.setSelection(
                if (loggedIn) {
                    defaultItem
                } else {
                    when (defaultItem) {
                        2 -> 1
                        3 -> 2
                        else -> 0
                    }
                }
            )
            firstLaunch = false
        }
        return if (loggedIn) {
            when (position) {
                0 -> FollowedStreamsFragment()
                1 -> FollowedVideosFragment()
                2 -> FollowedChannelsFragment()
                else -> FollowedGamesFragment()
            }
        } else {
            when (position) {
                0 -> FollowedStreamsFragment()
                1 -> FollowedChannelsFragment()
                else -> FollowedGamesFragment()
            }
        }
    }
}