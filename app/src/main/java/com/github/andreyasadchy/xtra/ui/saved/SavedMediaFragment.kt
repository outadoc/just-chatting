package com.github.andreyasadchy.xtra.ui.saved

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.MediaFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.saved.bookmarks.BookmarksFragment
import com.github.andreyasadchy.xtra.ui.saved.downloads.DownloadsFragment
import com.github.andreyasadchy.xtra.util.visible
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val pagerFragment = requireArguments().getBoolean(PAGER_FRAGMENT)
        val defaultItem = requireArguments().getInt(DEFAULT_ITEM)
        if (pagerFragment) {
            childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, SavedPagerFragment.newInstance(defaultItem)).commit()
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