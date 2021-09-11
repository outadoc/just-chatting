package com.github.andreyasadchy.xtra.ui.top

import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.ui.clips.common.ClipsFragment
import com.github.andreyasadchy.xtra.ui.common.MediaFragment
import com.github.andreyasadchy.xtra.ui.streams.common.StreamsFragment
import com.github.andreyasadchy.xtra.ui.videos.top.TopVideosFragment

class TopFragment : MediaFragment() {

    override fun onSpinnerItemSelected(position: Int): Fragment {
        return when (position) {
            0 -> StreamsFragment()
            1 -> TopVideosFragment()
            else -> ClipsFragment()
        }
    }
}