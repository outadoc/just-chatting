package fr.outadoc.justchatting.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ncapdevi.fragnav.FragNavController
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.ActivityMainBinding
import fr.outadoc.justchatting.ui.chat.ChatActivity
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.ui.follow.FollowMediaFragment
import fr.outadoc.justchatting.ui.login.LoginActivity
import fr.outadoc.justchatting.ui.search.SearchFragment
import fr.outadoc.justchatting.ui.settings.SettingsActivity
import fr.outadoc.justchatting.util.observeEvent
import fr.outadoc.justchatting.util.parseChannelLogin
import fr.outadoc.justchatting.util.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity(), NavigationHandler {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
            }
        }
    }

    private val viewModel: MainViewModel by viewModel()
    private lateinit var viewHolder: ActivityMainBinding

    private val fragNavController = FragNavController(
        supportFragmentManager,
        R.id.fragmentContainer
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewHolder = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewHolder.root)

        initNavigation()
        fragNavController.initialize(savedInstanceState = savedInstanceState)

        viewModel.events.observeEvent(this) { destination ->
            when (destination) {
                is MainViewModel.Destination.Channel -> {
                    startActivity(
                        ChatActivity.createIntent(
                            context = this,
                            channelLogin = destination.login
                        )
                    )
                }
                is MainViewModel.Destination.Login -> {
                    if (destination.causedByTokenExpiration) {
                        toast(R.string.token_expired)
                    }

                    startActivity(Intent(this, LoginActivity::class.java))
                }
                MainViewModel.Destination.Settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                MainViewModel.Destination.Search -> {
                    fragNavController.pushFragment(SearchFragment())
                }
            }
        }

        if (savedInstanceState == null) {
            intent.parseChannelFromIntent()?.let { login ->
                viewModel.onViewChannelRequest(login)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNavController.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.parseChannelFromIntent()?.let { login ->
            viewModel.onViewChannelRequest(login)
        }
    }

    private fun Intent.parseChannelFromIntent(): String? {
        if (action != Intent.ACTION_VIEW) return null
        return data?.parseChannelLogin()
    }

    override fun viewChannel(login: String) {
        viewModel.onViewChannelRequest(login)
    }

    override fun openSearch() {
        viewModel.onOpenSearchRequested()
    }

    override fun openSettings() {
        viewModel.onOpenSettingsRequested()
    }

    override fun onBackPressed() {
        if (fragNavController.isRootFragment || !fragNavController.popFragment()) {
            super.onBackPressed()
        }
    }

    private fun initNavigation() {
        fragNavController.apply {
            rootFragments = listOf(FollowMediaFragment.newInstance())
            fragmentHideStrategy = FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH
            transactionListener = object : FragNavController.TransactionListener {
                override fun onFragmentTransaction(
                    fragment: Fragment?,
                    transactionType: FragNavController.TransactionType
                ) {
                }

                override fun onTabTransaction(fragment: Fragment?, index: Int) {
                }
            }
        }
    }
}
