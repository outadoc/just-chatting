package com.github.andreyasadchy.xtra.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.chat.ChatNotificationUtils
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.follow.FollowMediaFragment
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.ui.search.SearchFragment
import com.github.andreyasadchy.xtra.ui.streams.BaseStreamsFragment
import com.ncapdevi.fragnav.FragNavController
import dagger.android.HasAndroidInjector

class MainActivity :
    BaseActivity(),
    BaseStreamsFragment.OnStreamSelectedListener,
    OnChannelSelectedListener,
    HasAndroidInjector,
    Injectable {

    private val viewModel by viewModels<MainViewModel> { viewModelFactory }

    private val fragNavController = FragNavController(
        supportFragmentManager,
        R.id.fragmentContainer
    )

    // Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.userToShow.observe(this) { user ->
            if (user != null && (!user.id.isNullOrBlank() || !user.login.isNullOrBlank())) {
                viewChannel(
                    id = user.id,
                    login = user.login,
                    name = user.display_name,
                    channelLogo = user.channelLogo
                )
            }
        }

        viewModel.currentUser.observe(this) { user ->
            if (user is User.NotLoggedIn) {
                startActivityForResult(
                    Intent(this, LoginActivity::class.java),
                    2
                )
                return@observe
            }

            initNavigation(isLoggedIn = user is User.LoggedIn)
            fragNavController.initialize(savedInstanceState = savedInstanceState)
        }

        viewModel.validate(activity = this)

        handleIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNavController.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Result of LoginActivity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        fun restartActivity() {
            finish()
            overridePendingTransition(0, 0)
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                ).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) }
            )
            overridePendingTransition(0, 0)
        }

        when (requestCode) {
            1 -> {
                // Was not logged in
                when (resultCode) {
                    // Logged in
                    RESULT_OK -> restartActivity()
                }
            }
            // Was logged in
            2 -> restartActivity()
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW || intent.data == null) return

        val url = intent.data.toString()
        val login = url.substringAfter("twitch.tv/").substringBefore("/")

        if (login.isNotBlank()) {
            viewModel.loadUser(login = login)
        }
    }

    // Navigation listeners

    override fun startStream(stream: Stream) {
        viewChannel(
            id = stream.user_id,
            login = stream.user_login,
            name = stream.user_name,
            channelLogo = stream.channelLogo
        )
    }

    override fun viewChannel(
        id: String?,
        login: String?,
        name: String?,
        channelLogo: String?,
        updateLocal: Boolean,
    ) {
        if (id == null || login == null || name == null || channelLogo == null) return
        ChatNotificationUtils.openInBubbleOrStartActivity(
            context = this@MainActivity,
            channelId = id,
            channelLogin = login,
            channelName = name,
            channelLogo = channelLogo
        )
    }

    override fun onBackPressed() {
        if (fragNavController.isRootFragment || !fragNavController.popFragment()) {
            super.onBackPressed()
        }
    }

    fun openSearch() {
        fragNavController.pushFragment(SearchFragment())
    }

    private fun initNavigation(isLoggedIn: Boolean) {
        fragNavController.apply {
            rootFragments = listOf(
                FollowMediaFragment.newInstance(loggedIn = isLoggedIn)
            )

            fragmentHideStrategy = FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH
            transactionListener = object : FragNavController.TransactionListener {
                override fun onFragmentTransaction(
                    fragment: Fragment?,
                    transactionType: FragNavController.TransactionType,
                ) {
                }

                override fun onTabTransaction(fragment: Fragment?, index: Int) {
                }
            }
        }
    }
}
