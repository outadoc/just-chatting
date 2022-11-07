package fr.outadoc.justchatting.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.ui.chat.ChatActivity
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.ui.login.LoginActivity
import fr.outadoc.justchatting.ui.settings.SettingsActivity
import fr.outadoc.justchatting.util.observeEvent
import fr.outadoc.justchatting.util.parseChannelLogin
import fr.outadoc.justchatting.util.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewMainActivity : BaseActivity(), NavigationHandler {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Mdc3Theme {
                HomeScreen(
                    onSearchClick = {
                        viewModel.onOpenSearchRequested()
                    },
                    onSettingsClick = {
                        viewModel.onOpenSettingsRequested()
                    },
                    onLogoutClick = {
                        onLogout()
                    },
                    onStreamClick = { stream ->
                        stream.userLogin?.let { login ->
                            viewChannel(login)
                        }
                    },
                    onFollowClick = { follow ->
                        follow.toLogin?.let { login ->
                            viewChannel(login)
                        }
                    }
                )
            }
        }

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

                }
            }
        }

        if (savedInstanceState == null) {
            intent.parseChannelFromIntent()?.let { login ->
                viewModel.onViewChannelRequest(login)
            }
        }
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

    private fun onLogout() {
        startActivity(
            Intent(this@NewMainActivity, LoginActivity::class.java)
        )
    }
}
