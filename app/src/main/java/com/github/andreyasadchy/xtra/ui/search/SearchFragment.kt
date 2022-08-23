package com.github.andreyasadchy.xtra.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.search.channels.ChannelSearchFragment
import com.github.andreyasadchy.xtra.util.showKeyboard
import kotlinx.android.synthetic.main.fragment_search.appBar
import kotlinx.android.synthetic.main.fragment_search.search
import kotlinx.android.synthetic.main.fragment_search.toolbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class SearchFragment : Fragment(), Scrollable {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    private var searchFragment: ChannelSearchFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.apply {
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener { activity?.onBackPressed() }
        }

        search.showKeyboard()

        searchFragment = ChannelSearchFragment().also { fragment ->
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }

        ViewCompat.setOnApplyWindowInsetsListener(appBar) { appBar, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())

            appBar.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )

            WindowInsetsCompat.CONSUMED
        }

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            private var job: Job? = null

            override fun onQueryTextSubmit(query: String): Boolean {
                searchFragment?.search(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                job?.cancel()
                if (newText.isNotEmpty()) {
                    job = lifecycleScope.launchWhenResumed {
                        delay(750)
                        searchFragment?.search(newText)
                    }
                } else {
                    searchFragment?.search(newText) // might be null on rotation, so as?
                }
                return false
            }
        })
    }

    override fun scrollToTop() {
        searchFragment?.scrollToTop()
    }
}
