package com.github.andreyasadchy.xtra.ui.common.pagers

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

abstract class ItemAwareFragmentPagerAdapter internal constructor(fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    internal var currentFragment: Fragment? = null
        private set

    private var listener: ((Fragment) -> Unit)? = null

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        val fragment = `object` as Fragment
        currentFragment = fragment
        listener?.invoke(fragment)
    }

    fun setOnItemChangedListener(listener: ((Fragment) -> Unit)?) {
        this.listener = listener
    }
}
