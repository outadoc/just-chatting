package com.github.andreyasadchy.xtra.ui.saved

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.pagers.ItemAwareFragmentPagerAdapter
import com.github.andreyasadchy.xtra.ui.saved.bookmarks.BookmarksFragment
import com.github.andreyasadchy.xtra.ui.saved.downloads.DownloadsFragment

class SavedPagerAdapter(
        private val context: Context,
        fm: FragmentManager) : ItemAwareFragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        val id = when (position) {
            0 -> R.string.bookmarks
            else -> R.string.downloads
        }
        return context.getString(id)
    }

    override fun getItem(position: Int): Fragment {
        val fragment: Fragment = when (position) {
            0 -> BookmarksFragment()
            else -> DownloadsFragment()
        }
        return fragment
    }

    override fun getCount(): Int = 2
}
