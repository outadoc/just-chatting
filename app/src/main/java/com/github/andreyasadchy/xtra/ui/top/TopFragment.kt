package com.github.andreyasadchy.xtra.ui.top

import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.ui.common.MediaFragment
import com.github.andreyasadchy.xtra.ui.streams.common.StreamsFragment

class TopFragment : MediaFragment() {

    override var hideSpinner = true

    override fun onSpinnerItemSelected(position: Int): Fragment {
        return StreamsFragment()
    }
}