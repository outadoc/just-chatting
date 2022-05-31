package com.github.andreyasadchy.xtra.ui.top

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.MediaFragment
import com.github.andreyasadchy.xtra.ui.streams.common.StreamsFragment

class TopFragment : MediaFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentFragment = if (previousItem != 0) {
            val newFragment = onSpinnerItemSelected(0)
            childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, newFragment).commit()
            previousItem = 0
            newFragment
        } else {
            childFragmentManager.findFragmentById(R.id.fragmentContainer)
        }
    }

    override fun onSpinnerItemSelected(position: Int): Fragment {
        return StreamsFragment()
    }
}