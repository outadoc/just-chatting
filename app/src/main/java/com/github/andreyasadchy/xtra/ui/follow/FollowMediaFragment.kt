package com.github.andreyasadchy.xtra.ui.follow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.MediaFragment
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsFragment
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsFragment
import kotlinx.android.synthetic.main.fragment_media.spinner

class FollowMediaFragment : MediaFragment() {

    companion object {
        private const val LOGGED_IN = "logged_in"

        fun newInstance(loggedIn: Boolean) =
            FollowMediaFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(LOGGED_IN, loggedIn)
                }
            }
    }

    private var firstLaunch = true

    override val spinnerItems: Array<String>
        get() = resources.getStringArray(R.array.spinnerFollowedEntries)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loggedIn = requireArguments().getBoolean(LOGGED_IN)

        currentFragment = if (previousItem != -2) {
            val newFragment = FollowPagerFragment.newInstance(loggedIn)
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, newFragment)
                .commit()

            previousItem = -2
            newFragment
        } else {
            childFragmentManager.findFragmentById(R.id.fragmentContainer)
        }
    }

    override fun onSpinnerItemSelected(position: Int): Fragment {
        if (firstLaunch) {
            spinner.setSelection(0)
            firstLaunch = false
        }
        return when (position) {
            0 -> FollowedStreamsFragment()
            else -> FollowedChannelsFragment()
        }
    }
}
