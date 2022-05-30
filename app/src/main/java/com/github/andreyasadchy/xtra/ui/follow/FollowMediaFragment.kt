package com.github.andreyasadchy.xtra.ui.follow

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.MediaFragment
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsFragment
import com.github.andreyasadchy.xtra.ui.follow.games.FollowedGamesFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsFragment
import com.github.andreyasadchy.xtra.ui.videos.followed.FollowedVideosFragment
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_media.*

class FollowMediaFragment : MediaFragment() {

    companion object {
        private const val FOLLOW_PAGER = "follow_pager"
        private const val DEFAULT_ITEM = "default_item"
        private const val LOGGED_IN = "logged_in"

        fun newInstance(followPager: Boolean, defaultItem: Int?, loggedIn: Boolean) = FollowMediaFragment().apply {
            arguments = Bundle().apply {
                putBoolean(FOLLOW_PAGER, followPager)
                putInt(DEFAULT_ITEM, defaultItem ?: 0)
                putBoolean(LOGGED_IN, loggedIn)
            }
        }
    }

    private var firstLaunch = true

    override val spinnerItems: Array<String>
        get() = resources.getStringArray(if (requireArguments().getBoolean(LOGGED_IN)) R.array.spinnerFollowedEntries else R.array.spinnerFollowedEntriesNotLoggedIn)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val followPager = requireArguments().getBoolean(FOLLOW_PAGER)
        val defaultItem = requireArguments().getInt(DEFAULT_ITEM)
        val loggedIn = requireArguments().getBoolean(LOGGED_IN)
        if (followPager) {
            childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, FollowPagerFragment.newInstance(defaultItem, loggedIn)).commit()
        } else {
            spinner.visible()
            spinner.adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, spinnerItems)
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    currentFragment = if (position != previousItem && isResumed) {
                        val newFragment = onSpinnerItemSelected(position)
                        childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, newFragment).commit()
                        previousItem = position
                        newFragment
                    } else {
                        childFragmentManager.findFragmentById(R.id.fragmentContainer)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun onSpinnerItemSelected(position: Int): Fragment {
        val defaultItem = requireArguments().getInt(DEFAULT_ITEM)
        val loggedIn = requireArguments().getBoolean(LOGGED_IN)
        if (firstLaunch) {
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