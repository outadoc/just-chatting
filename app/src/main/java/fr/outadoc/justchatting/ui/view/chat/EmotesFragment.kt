package fr.outadoc.justchatting.ui.view.chat

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
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import fr.outadoc.justchatting.databinding.FragmentEmotesBinding
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.ui.chat.ChatViewModel
import fr.outadoc.justchatting.ui.common.Scrollable
import fr.outadoc.justchatting.ui.view.GridAutofitLayoutManager
import fr.outadoc.justchatting.util.convertDpToPixels
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmotesFragment : Fragment(), Scrollable {

    private var listener: OnEmoteClickedListener? = null
    private lateinit var layoutManager: GridAutofitLayoutManager

    private val chatPreferencesRepository: ChatPreferencesRepository by inject()

    private val viewModel: ChatViewModel by viewModel()
    private var viewHolder: FragmentEmotesBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnEmoteClickedListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewHolder = FragmentEmotesBinding.inflate(layoutInflater, container, false)
        return viewHolder?.root
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
        viewHolder?.recyclerViewEmotes?.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewHolder = null
    }
}
