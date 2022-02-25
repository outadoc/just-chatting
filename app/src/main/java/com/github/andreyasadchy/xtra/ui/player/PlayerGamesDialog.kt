package com.github.andreyasadchy.xtra.ui.player

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment


class PlayerGamesDialog : ExpandingBottomSheetDialogFragment() {

    interface PlayerSeekListener {
        fun seek(position: Long)
    }

    companion object {

        private const val GAMES_LIST = "gamesList"

        fun newInstance(gamesList: List<Game>): PlayerGamesDialog {
            return PlayerGamesDialog().apply {
                arguments = bundleOf(GAMES_LIST to gamesList)
            }
        }
    }

    private lateinit var listener: PlayerSeekListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as PlayerSeekListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()
        val arguments = requireArguments()
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val radioGroup = RadioGroup(context).also { it.layoutParams = layoutParams }
        arguments.getParcelableArrayList<Game>(GAMES_LIST)?.forEach { game ->
            val button = AppCompatRadioButton(context).apply {
                buttonDrawable = null
                text = game.name
                setOnClickListener {
                    game.vodPosition?.let { position -> listener.seek(position.toLong()) }
                    dismiss()
                }
            }
            radioGroup.addView(button, layoutParams)
        }
        val scrollView = NestedScrollView(context)
        scrollView.addView(radioGroup)
        return scrollView
    }
}
