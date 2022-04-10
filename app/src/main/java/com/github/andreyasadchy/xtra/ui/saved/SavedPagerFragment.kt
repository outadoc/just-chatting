package com.github.andreyasadchy.xtra.ui.saved

import android.os.Bundle
import android.view.View
import com.github.andreyasadchy.xtra.ui.common.pagers.MediaPagerFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity

class SavedPagerFragment : MediaPagerFragment() {

    companion object {
        private const val DEFAULT_ITEM = "default_item"

        fun newInstance(defaultItem: Int?) = SavedPagerFragment().apply {
            arguments = Bundle().apply {
                putInt(DEFAULT_ITEM, defaultItem ?: 0)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val defaultItem = requireArguments().getInt(DEFAULT_ITEM)
        setAdapter(adapter = SavedPagerAdapter(activity, childFragmentManager), currentItem = defaultItem)
    }

    override fun initialize() {
    }

    override fun onNetworkRestored() {
    }
}