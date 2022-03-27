package com.github.andreyasadchy.xtra.ui.follow

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.pagers.ItemAwareFragmentPagerAdapter
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsFragment
import com.github.andreyasadchy.xtra.ui.follow.games.FollowedGamesFragment
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsFragment
import com.github.andreyasadchy.xtra.ui.videos.followed.FollowedVideosFragment

class FollowPagerAdapter(
        private val context: Context,
        fm: FragmentManager,
        private val loggedIn: Boolean) : ItemAwareFragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        val id = if (loggedIn) {
            when (position) {
                0 -> R.string.games
                1 -> R.string.live
                2 -> R.string.videos
                else -> R.string.channels
            }
        } else {
            when (position) {
                0 -> R.string.games
                1 -> R.string.live
                else -> R.string.channels
            }
        }
        return context.getString(id)
    }

    override fun getItem(position: Int): Fragment {
        val fragment: Fragment = if (loggedIn) {
            when (position) {
                0 -> FollowedGamesFragment()
                1 -> FollowedStreamsFragment()
                2 -> FollowedVideosFragment()
                else -> FollowedChannelsFragment()
            }
        } else {
            when (position) {
                0 -> FollowedGamesFragment()
                1 -> FollowedStreamsFragment()
                else -> FollowedChannelsFragment()
            }
        }
        return fragment
    }

    override fun getCount(): Int = if (loggedIn) 4 else 3
}
