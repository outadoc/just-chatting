package com.github.andreyasadchy.xtra.ui.view.chat

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.repository.ChatPreferencesRepository
import com.github.andreyasadchy.xtra.ui.chat.ChatViewModel
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.view.GridAutofitLayoutManager
import com.github.andreyasadchy.xtra.util.convertDpToPixels
import javax.inject.Inject

class EmotesFragment : Fragment(), Injectable, Scrollable {

    private var listener: OnEmoteClickedListener? = null
    private lateinit var layoutManager: GridAutofitLayoutManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var chatPreferencesRepository: ChatPreferencesRepository

    private val viewModel by activityViewModels<ChatViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnEmoteClickedListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_emotes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val args = requireArguments()

        val emotesAdapter = EmotesAdapter(
            clickListener = { emote -> listener?.onEmoteClicked(emote) }
        )

        chatPreferencesRepository.animateEmotes
            .asLiveData()
            .observe(viewLifecycleOwner) { animateEmotes ->
                emotesAdapter.animateEmotes = animateEmotes
            }

        with(view as RecyclerView) {
            itemAnimator = null
            adapter = emotesAdapter
            layoutManager = GridAutofitLayoutManager(
                context = context,
                columnWidth = context.convertDpToPixels(50f)
            ).also { layoutManager ->
                layoutManager.isHeaderLookup = { position ->
                    when (emotesAdapter.getItemViewType(position)) {
                        EmotesAdapter.TYPE_HEADER -> true
                        else -> false
                    }
                }

                this@EmotesFragment.layoutManager = layoutManager
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            emotesAdapter.submitList(
                when (args.getInt(KEY_POSITION)) {
                    0 -> state.recentEmotes
                    1 -> state.twitchEmotes
                    else -> state.otherEmotes
                }.toList()
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val navBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

            v.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                navBarInsets.bottom
            )

            windowInsets
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        layoutManager.updateWidth()
    }

    companion object {
        private const val KEY_POSITION = "position"

        fun newInstance(position: Int) =
            EmotesFragment().apply {
                arguments = bundleOf(KEY_POSITION to position)
            }
    }

    override fun scrollToTop() {
        (view as? RecyclerView)?.smoothScrollToPosition(0)
    }
}
