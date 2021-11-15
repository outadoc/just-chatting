package com.github.andreyasadchy.xtra.ui.games

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_games_list_item.view.*

class GamesAdapter(
        private val fragment: Fragment,
        private val listener: GamesFragment.OnGameSelectedListener) : BasePagedListAdapter<Game>(
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
            gameImage.loadImage(fragment, TwitchApiHelper.getTemplateUrl(item.box_art_url, "medium", true))
            gameName.text = item.name
            viewers.text = TwitchApiHelper.formatViewersCount(context, 0, context.prefs().getBoolean(C.UI_VIEWCOUNT, false))
        }
    }
}
