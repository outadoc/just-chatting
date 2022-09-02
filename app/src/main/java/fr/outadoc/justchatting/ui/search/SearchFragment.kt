package fr.outadoc.justchatting.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.FragmentSearchBinding
import fr.outadoc.justchatting.ui.common.Scrollable
import fr.outadoc.justchatting.ui.search.channels.ChannelSearchFragment
import fr.outadoc.justchatting.util.showKeyboard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class SearchFragment : Fragment(), Scrollable {

    private var viewHolder: FragmentSearchBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewHolder = FragmentSearchBinding.inflate(inflater, container, false)
        return viewHolder?.root
    }

    private var searchFragment: ChannelSearchFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchFragment = ChannelSearchFragment().also { fragment ->
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }

        viewHolder?.apply {
            toolbar.apply {
                setNavigationIcon(R.drawable.ic_back)
                setNavigationOnClickListener { activity?.onBackPressed() }
            }

            search.showKeyboard()

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
    }

    override fun scrollToTop() {
        searchFragment?.scrollToTop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewHolder = null
    }
}
