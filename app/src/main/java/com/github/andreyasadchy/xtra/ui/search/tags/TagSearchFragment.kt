package com.github.andreyasadchy.xtra.ui.search.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.tag.Tag
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.PagedListFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.search.Searchable
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

class TagSearchFragment : PagedListFragment<Tag, TagSearchViewModel, BasePagedListAdapter<Tag>>(), Searchable {

    override val viewModel by viewModels<TagSearchViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Tag> by lazy { TagSearchAdapter(this, requireActivity() as MainActivity, requireActivity() as MainActivity) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.common_recycler_view_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefresh.isEnabled = false
        viewModel.loadTags(clientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), getGameTags = parentFragment?.arguments?.getBoolean(C.GET_GAME_TAGS) ?: false, gameId = parentFragment?.arguments?.getString(C.GAME_ID), gameName = parentFragment?.arguments?.getString(C.GAME_NAME))
    }

    override fun search(query: String) {
        viewModel.setQuery(query)
    }
}