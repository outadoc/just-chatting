package com.github.andreyasadchy.xtra.ui.common.pagers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.BaseNetworkFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import kotlinx.android.synthetic.main.fragment_media_pager.viewPager

abstract class MediaPagerFragment : BaseNetworkFragment(), ItemAwarePagerFragment, Scrollable {

    private lateinit var adapter: ItemAwareFragmentPagerAdapter

    override val currentFragment: Fragment?
        get() = adapter.currentFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media_pager, container, false)
    }

    protected fun setAdapter(adapter: ItemAwareFragmentPagerAdapter, currentItem: Int? = null) {
        this.adapter = adapter
        viewPager.adapter = adapter
        if (currentItem != null) {
            viewPager.currentItem = currentItem
        }
        viewPager.offscreenPageLimit = adapter.count
    }

    override fun scrollToTop() {
        if (currentFragment is Scrollable) {
            (currentFragment as Scrollable).scrollToTop()
        }
    }
}
