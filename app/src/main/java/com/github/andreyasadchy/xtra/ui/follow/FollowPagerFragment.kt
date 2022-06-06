package com.github.andreyasadchy.xtra.ui.follow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.BaseNetworkFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.common.pagers.ItemAwareFragmentPagerAdapter
import com.github.andreyasadchy.xtra.ui.common.pagers.ItemAwarePagerFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_media_pager.viewPager

class FollowPagerFragment : BaseNetworkFragment(), ItemAwarePagerFragment, Scrollable {

    companion object {
        private const val LOGGED_IN = "logged_in"

        fun newInstance(loggedIn: Boolean) = FollowPagerFragment().apply {
            arguments = Bundle().apply {
                putBoolean(LOGGED_IN, loggedIn)
            }
        }
    }
    private var adapter: ItemAwareFragmentPagerAdapter? = null

    override val currentFragment: Fragment?
        get() = adapter?.currentFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        setAdapter(FollowPagerAdapter(activity, childFragmentManager))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media_pager, container, false)
    }

    private fun setAdapter(adapter: ItemAwareFragmentPagerAdapter) {
        this.adapter = adapter
        viewPager.adapter = adapter
        viewPager.currentItem = 0
        viewPager.offscreenPageLimit = adapter.count
    }

    override fun initialize() {
    }

    override fun onNetworkRestored() {
    }

    override fun scrollToTop() {
        if (currentFragment is Scrollable) {
            (currentFragment as Scrollable).scrollToTop()
        }
    }
}
