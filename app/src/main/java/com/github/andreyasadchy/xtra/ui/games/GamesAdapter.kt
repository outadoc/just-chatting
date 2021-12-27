package com.github.andreyasadchy.xtra.ui.games

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnGameSelectedListener
import com.github.andreyasadchy.xtra.util.*
import kotlinx.android.synthetic.main.fragment_games_list_item.view.*

class GamesAdapter(
        private val fragment: Fragment,
        private val listener: OnGameSelectedListener) : BasePagedListAdapter<Game>(
        object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean =
                    true
        }) {

    override val layoutId: Int = R.layout.fragment_games_list_item

    override fun bind(item: Game, view: View) {
        with(view) {
            setOnClickListener { listener.openGame(item.id, item.name) }
            if (item.boxArt != null)  {
                gameImage.visible()
                gameImage.loadImage(fragment, item.boxArt)
            }
            if (item.name != null)  {
                gameName.visible()
                gameName.text = item.name
            }
            if (item.viewersCount != null)  {
                viewers.visible()
                viewers.text = TwitchApiHelper.formatViewersCount(context, item.viewersCount, context.prefs().getBoolean(C.UI_VIEWCOUNT, false))
            }
            if (item.broadcastersCount != null && context.prefs().getBoolean(C.UI_BROADCASTERSCOUNT, true)) {
                broadcastersCount.visible()
                broadcastersCount.text = context.getString(R.string.broadcasters, item.broadcastersCount.toString())
            }
        }
    }
}
