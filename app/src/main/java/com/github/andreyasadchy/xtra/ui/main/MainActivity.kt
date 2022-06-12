package com.github.andreyasadchy.xtra.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.chat.ChatNotificationUtils
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.follow.FollowMediaFragment
import com.github.andreyasadchy.xtra.ui.search.SearchFragment
import com.github.andreyasadchy.xtra.ui.streams.BaseStreamsFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.isNetworkAvailable
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.shortToast
import com.ncapdevi.fragnav.FragNavController
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.launch

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

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.setNetworkAvailable(isNetworkAvailable)
        }
    }

    // Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val notInitialized = savedInstanceState == null

        initNavigation()
        fragNavController.initialize(savedInstanceState = savedInstanceState)

        val prefs = prefs()
        var flag = notInitialized && !isNetworkAvailable

        viewModel.isNetworkAvailable.observe(this) {
            it.getContentIfNotHandled()?.let { online ->
                if (online) {
                    viewModel.validate(
                        prefs.getString(C.HELIX_CLIENT_ID, ""),
                        prefs.getString(C.GQL_CLIENT_ID, ""),
                        this
                    )
                }
                if (flag) {
                    shortToast(if (online) R.string.connection_restored else R.string.no_connection)
                } else {
                    flag = true
                }
            }
        }

        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        handleIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNavController.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        unregisterReceiver(networkReceiver)
        super.onDestroy()
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
            val prefs = prefs()
            viewModel.loadUser(
                login,
                prefs.getString(C.HELIX_CLIENT_ID, ""),
                prefs.getString(C.TOKEN, ""),
                prefs.getString(C.GQL_CLIENT_ID, "")
            )

            viewModel.user.observe(this) { user ->
                if (user != null && (!user.id.isNullOrBlank() || !user.login.isNullOrBlank())) {
                    viewChannel(
                        id = user.id,
                        login = user.login,
                        name = user.display_name,
                        channelLogo = user.channelLogo
                    )
                }
            }
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
        updateLocal: Boolean
    ) {
        if (id == null || login == null || name == null || channelLogo == null) return
        lifecycleScope.launch {
            ChatNotificationUtils.openInBubbleOrStartActivity(
                context = this@MainActivity,
                channelId = id,
                channelLogin = login,
                channelName = name,
                channelLogo = channelLogo
            )
        }
    }

    override fun onBackPressed() {
        if (!fragNavController.popFragment()) {
            super.onBackPressed()
        }
    }

    fun openSearch() {
        fragNavController.pushFragment(SearchFragment())
    }

    private fun initNavigation() {
        fragNavController.apply {
            rootFragments = listOf(
                FollowMediaFragment.newInstance(
                    loggedIn = !User.get(this@MainActivity).gqlToken.isNullOrBlank()
                ),
            )
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
