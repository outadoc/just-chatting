package com.github.andreyasadchy.xtra.ui.saved

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.MediaFragment
import com.github.andreyasadchy.xtra.ui.saved.bookmarks.BookmarksFragment
import com.github.andreyasadchy.xtra.ui.saved.downloads.DownloadsFragment
import kotlinx.android.synthetic.main.fragment_media.*

class SavedMediaFragment : MediaFragment() {

    companion object {
        private const val PAGER_FRAGMENT = "pager_fragment"
        private const val DEFAULT_ITEM = "default_item"

        fun newInstance(pagerFragment: Boolean, defaultItem: Int?) = SavedMediaFragment().apply {
            arguments = Bundle().apply {
                putBoolean(PAGER_FRAGMENT, pagerFragment)
                putInt(DEFAULT_ITEM, defaultItem ?: 0)
            }
        }
    }

    private var firstLaunch = true

    override val spinnerItems: Array<String>
        get() = resources.getStringArray(R.array.spinnerSavedEntries)

    override fun onSpinnerItemSelected(position: Int): Fragment {
        val pagerFragment = requireArguments().getBoolean(PAGER_FRAGMENT)
        val defaultItem = requireArguments().getInt(DEFAULT_ITEM)
        if (pagerFragment) {
            hideSpinner = true
            return SavedPagerFragment.newInstance(defaultItem)
        }
        if (firstLaunch) {
            spinner.setSelection(defaultItem)
            firstLaunch = false
        }
        return when (position) {
            0 -> BookmarksFragment()
            else -> DownloadsFragment()
        }
    }
}