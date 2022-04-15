package com.github.andreyasadchy.xtra.ui.search.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.Utils
import com.github.andreyasadchy.xtra.ui.common.pagers.MediaPagerFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.search.Searchable
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.gone
import kotlinx.android.synthetic.main.fragment_media_pager.view.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class BaseTagSearchFragment : MediaPagerFragment() {

    companion object {
        fun newInstance(getGameTags: Boolean, gameId: String?, gameName: String?) = BaseTagSearchFragment().apply {
            arguments = Bundle().apply {
                putBoolean(C.GET_GAME_TAGS, getGameTags)
                putString(C.GAME_ID, gameId)
                putString(C.GAME_NAME, gameName)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val adapter = BaseTagSearchPagerAdapter(activity, childFragmentManager).apply {
            setOnItemChangedListener {
                if (it.isResumed) {
                    (it as Searchable).search(search.query.toString())
                }
            }
        }
        setAdapter(adapter)
        toolbar.apply {
            navigationIcon = Utils.getNavigationIcon(activity)
            setNavigationOnClickListener { activity.popFragment() }
        }
        fragmentContainer.viewPager.tabLayout.gone()
    }

    override fun initialize() {
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            private var job: Job? = null

            override fun onQueryTextSubmit(query: String): Boolean {
                (currentFragment as? Searchable)?.search(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                job?.cancel()
                if (newText.isNotEmpty()) {
                    job = lifecycleScope.launchWhenResumed {
                        delay(750)
                        (currentFragment as? Searchable)?.search(newText)
                    }
                } else {
                    (currentFragment as? Searchable)?.search(newText) //might be null on rotation, so as?
                }
                return false
            }
        })
    }

    override fun onNetworkRestored() {

    }
}