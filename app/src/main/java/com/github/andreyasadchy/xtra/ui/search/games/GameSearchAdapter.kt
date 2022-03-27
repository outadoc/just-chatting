package com.github.andreyasadchy.xtra.ui.search.games

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.games.GamesFragment
import com.github.andreyasadchy.xtra.util.*
import kotlinx.android.synthetic.main.fragment_games_list_item.view.*

class GameSearchAdapter(
        private val fragment: Fragment,
        private val listener: GamesFragment.OnGameSelectedListener,
        private val gamesListener: GamesFragment.OnTagGames) : BasePagedListAdapter<Game>(
        object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean = true
        }) {

    override val layoutId: Int = R.layout.fragment_games_list_item

    override fun bind(item: Game, view: View) {
        with(view) {
            setOnClickListener { listener.openGame(item.id, item.name) }
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
            if (item.viewersCount != null) {
                viewers.visible()
                viewers.text = TwitchApiHelper.formatViewersCount(context, item.viewersCount!!)
            } else {
                viewers.gone()
            }
            if (item.broadcastersCount != null && context.prefs().getBoolean(C.UI_BROADCASTERSCOUNT, true)) {
                broadcastersCount.visible()
                broadcastersCount.text = resources.getQuantityString(R.plurals.broadcasters, item.broadcastersCount!!, item.broadcastersCount)
            } else {
                broadcastersCount.gone()
            }
            if (item.tags != null && context.prefs().getBoolean(C.UI_TAGS, true)) {
                tagsLayout.removeAllViews()
                tagsLayout.visible()
                for (tag in item.tags!!) {
                    val text = TextView(context)
                    text.text = tag.name
                    if (tag.id != null) {
                        text.setOnClickListener { gamesListener.openTagGames(listOf(tag.id)) }
                    }
                    tagsLayout.addView(text)
                }
            } else {
                tagsLayout.gone()
            }
        }
    }
}


