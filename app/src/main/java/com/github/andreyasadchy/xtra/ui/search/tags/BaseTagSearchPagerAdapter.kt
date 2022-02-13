package com.github.andreyasadchy.xtra.ui.search.tags

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.pagers.ItemAwareFragmentPagerAdapter

class BaseTagSearchPagerAdapter(
        private val context: Context,
        fm: FragmentManager) : ItemAwareFragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(R.string.channels)
    }

    override fun getItem(position: Int): Fragment {
        return TagSearchFragment()
    }

    override fun getCount(): Int = 1
}
