package com.github.andreyasadchy.xtra.ui.games

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.PagedListFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.settings.SettingsActivity
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.fragment_channel.*
import kotlinx.android.synthetic.main.fragment_games.*
import kotlinx.android.synthetic.main.fragment_games.appBar
import kotlinx.android.synthetic.main.fragment_games.menu
import kotlinx.android.synthetic.main.fragment_games.search
import kotlinx.android.synthetic.main.fragment_media.*

class GamesFragment : PagedListFragment<Game, GamesViewModel, BasePagedListAdapter<Game>>(), Scrollable {

    interface OnGameSelectedListener {
        fun openGame(id: String?, name: String?)
    }

    override val viewModel by viewModels<GamesViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Game> by lazy { GamesAdapter(this, requireActivity() as MainActivity) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_games, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val isLoggedIn = User.get(activity) !is NotLoggedIn
        search.setOnClickListener { activity.openSearch() }
        menu.setOnClickListener { it ->
            PopupMenu(activity, it).apply {
                inflate(R.menu.top_menu)
                menu.findItem(R.id.login).title = if (isLoggedIn) getString(R.string.log_out) else getString(R.string.log_in)
                setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.settings -> { activity.startActivityFromFragment(this@GamesFragment, Intent(activity, SettingsActivity::class.java), 3) }
                        R.id.login -> {
                            if (!isLoggedIn) {
                                activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 1)
                            } else {
                                AlertDialog.Builder(activity)
                                    .setTitle(getString(R.string.logout_title))
                                    .setMessage(getString(R.string.logout_msg, context?.prefs()?.getString(C.USERNAME, "")))
                                    .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
                                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                        activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 2) }
                                    .show()
                            }
                        }
                        else -> menu.close()
                    }
                    true
                }
                show()
            }
        }
    }

    override fun scrollToTop() {
        appBar?.setExpanded(true, true)
        recyclerView?.scrollToPosition(0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            requireActivity().recreate()
        }
    }

    override fun initialize() {
        super.initialize()
        if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
            viewModel.loadGames(true, requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), requireContext().prefs().getString(C.TOKEN, ""))
        } else {
            viewModel.loadGames(false, requireContext().prefs().getString(C.GQL_CLIENT_ID, ""))}
    }
}