package com.github.andreyasadchy.xtra.ui.player.games

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.player.PlayerGamesDialog
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.fragment_games_list_item.view.*

class PlayerGamesAdapter(
        private val fragment: Fragment) : BasePagedListAdapter<Game>(
        object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean =
                    oldItem.vodPosition == newItem.vodPosition

            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean = true
        }) {

    override val layoutId: Int = R.layout.fragment_games_list_item

    override fun bind(item: Game, view: View) {
        with(view) {
            setOnClickListener {
                item.vodPosition?.let { position -> (fragment.parentFragment as? PlayerGamesDialog)?.listener?.seek(position.toLong()) }
                (fragment.parentFragment as? PlayerGamesDialog)?.dismiss()
            }
            if (item.boxArt != null)  {
                gameImage.visible()
                gameImage.loadImage(fragment, item.boxArt)
            } else {
                gameImage.gone()
            }
            if (item.name != null)  {
                gameName.visible()
                gameName.text = item.name
            } else {
                gameName.gone()
            }
            val position = TwitchApiHelper.getDurationFromSeconds(context, (item.vodPosition?.div(1000)).toString(), true)
            if (item.vodPosition != null && position?.isNotBlank() == true) {
                viewers.visible()
                viewers.text = context.getString(R.string.position, position)
            } else {
                viewers.gone()
            }
            val duration = TwitchApiHelper.getDurationFromSeconds(context, (item.vodDuration?.div(1000)).toString(), true)
            if (item.vodDuration != null && duration?.isNotBlank() == true) {
                broadcastersCount.visible()
                broadcastersCount.text = context.getString(R.string.duration, duration)
            } else {
                broadcastersCount.gone()
            }
        }
    }
}