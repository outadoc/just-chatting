package com.github.andreyasadchy.xtra.ui.follow

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.pagers.ItemAwareFragmentPagerAdapter
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsFragment
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsFragment

class FollowPagerAdapter(
    private val context: Context,
    fm: FragmentManager
) : ItemAwareFragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence {
        val id = when (position) {
            0 -> R.string.live
            else -> R.string.channels
        }
        return context.getString(id)
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FollowedStreamsFragment()
            else -> FollowedChannelsFragment()
        }
    }

    override fun getCount(): Int = 2
}
