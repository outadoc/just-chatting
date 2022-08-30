package com.github.andreyasadchy.xtra.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.ui.chat.ChatNotificationUtils
import com.github.andreyasadchy.xtra.ui.common.NavigationHandler
import com.github.andreyasadchy.xtra.ui.follow.FollowMediaFragment
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.ui.search.SearchFragment
import com.github.andreyasadchy.xtra.ui.settings.SettingsActivity
import com.github.andreyasadchy.xtra.util.observeEvent
import com.github.andreyasadchy.xtra.util.toast
import com.ncapdevi.fragnav.FragNavController
import dagger.android.HasAndroidInjector

class MainActivity : BaseActivity(), NavigationHandler, HasAndroidInjector, Injectable {

    private companion object {
        const val REQUEST_CODE_LOGIN = 2
        const val REQUEST_CODE_SETTINGS = 3
    }

    private val viewModel by viewModels<MainViewModel> { viewModelFactory }

    private val fragNavController = FragNavController(
        supportFragmentManager,
        R.id.fragmentContainer
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.events.observeEvent(this) { event ->
            when (event) {
                MainViewModel.State.Loading -> {}
                is MainViewModel.State.NavigateTo -> {
                    when (event.destination) {
                        MainViewModel.Destination.Home -> {
                            initNavigation()
                            fragNavController.initialize(savedInstanceState = savedInstanceState)
                        }
                        is MainViewModel.Destination.Channel -> {
                            ChatNotificationUtils.openInBubbleOrStartActivity(
                                context = this,
                                channelId = event.destination.id,
                                channelLogin = event.destination.login,
                                channelName = event.destination.name,
                                channelLogo = event.destination.channelLogo
                            )
                        }
                        is MainViewModel.Destination.Login -> {
                            if (event.destination.causedByTokenExpiration) {
                                toast(R.string.token_expired)
                            }

                            startActivityForResult(
                                Intent(this, LoginActivity::class.java),
                                REQUEST_CODE_LOGIN
                            )
                        }
                        MainViewModel.Destination.Settings -> {
                            startActivityForResult(
                                Intent(this, SettingsActivity::class.java),
                                REQUEST_CODE_SETTINGS
                            )
                        }
                        MainViewModel.Destination.Search -> {
                            fragNavController.pushFragment(SearchFragment())
                        }
                    }
                }
            }
        }

        intent.parseChannelFromIntent()?.let { login ->
            viewModel.onViewChannelRequest(login)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNavController.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent.parseChannelFromIntent()
    }

    private fun Intent?.parseChannelFromIntent(): String? {
        if (this?.action != Intent.ACTION_VIEW || data == null) return null

        val url = data.toString()
        return url.substringAfter("twitch.tv/")
            .substringBefore("/")
            .takeIf { it.isNotBlank() }
    }

    override fun viewChannel(id: String?, login: String?, name: String?, channelLogo: String?) {
        viewModel.onViewChannelRequest(id, login, name, channelLogo)
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
