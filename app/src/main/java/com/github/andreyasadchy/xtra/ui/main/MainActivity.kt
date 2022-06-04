package com.github.andreyasadchy.xtra.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.channel.ChannelPagerFragment
import com.github.andreyasadchy.xtra.ui.chat.ChatFragment
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.follow.FollowMediaFragment
import com.github.andreyasadchy.xtra.ui.search.SearchFragment
import com.github.andreyasadchy.xtra.ui.streams.BaseStreamsFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.applyTheme
import com.github.andreyasadchy.xtra.util.isNetworkAvailable
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.shortToast
import com.ncapdevi.fragnav.FragNavController
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class MainActivity : AppCompatActivity(), BaseStreamsFragment.OnStreamSelectedListener, OnChannelSelectedListener, HasAndroidInjector, Injectable {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MainViewModel> { viewModelFactory }

    private val fragNavController = FragNavController(supportFragmentManager, R.id.fragmentContainer)
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.setNetworkAvailable(isNetworkAvailable)
        }
    }
    private lateinit var prefs: SharedPreferences

    // Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = prefs()
        if (prefs.getBoolean(C.FIRST_LAUNCH2, true)) {
            PreferenceManager.setDefaultValues(this@MainActivity, R.xml.root_preferences, false)
            PreferenceManager.setDefaultValues(this@MainActivity, R.xml.api_preferences, true)
            prefs.edit {
                putBoolean(C.FIRST_LAUNCH2, false)
            }
        }
        if (prefs.getBoolean(C.FIRST_LAUNCH, true)) {
            prefs.edit {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    putString(C.CHAT_IMAGE_LIBRARY, "2")
                }
                putBoolean(C.FIRST_LAUNCH, false)
            }
        }
        if (prefs.getBoolean(C.FIRST_LAUNCH1, true)) {
            prefs.edit {
                putBoolean(C.FIRST_LAUNCH1, false)
            }
        }
        applyTheme()
        setContentView(R.layout.activity_main)

        val notInitialized = savedInstanceState == null
        initNavigation()

        fragNavController.initialize(savedInstanceState = savedInstanceState)

        var flag = notInitialized && !isNetworkAvailable
        viewModel.isNetworkAvailable.observe(this) {
            it.getContentIfNotHandled()?.let { online ->
                if (online) {
                    viewModel.validate(prefs.getString(C.HELIX_CLIENT_ID, ""), prefs.getString(C.GQL_CLIENT_ID, ""), this)
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
            startActivity(Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
            overridePendingTransition(0, 0)
        }
        when (requestCode) {
            1 -> { // Was not logged in
                when (resultCode) { // Logged in
                    RESULT_OK -> restartActivity()
                }
            }
            2 -> restartActivity() // Was logged in
        }
    }

    override fun onBackPressed() {
        if (fragNavController.isRootFragment) {
            super.onBackPressed()
        } else {
            val currentFrag = fragNavController.currentFrag
            if (currentFrag !is ChannelPagerFragment || (currentFrag.currentFragment.let { it !is ChatFragment || !it.hideEmotesMenu() })) {
                fragNavController.popFragment()
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val url = intent.data.toString()
            when {
                else -> {
                    val login = url.substringAfter("twitch.tv/").substringBefore("/")
                    if (login.isNotBlank()) {
                        viewModel.loadUser(login, prefs.getString(C.HELIX_CLIENT_ID, ""), prefs.getString(C.TOKEN, ""), prefs.getString(C.GQL_CLIENT_ID, ""))
                        viewModel.user.observe(this) { user ->
                            if (user != null && (!user.id.isNullOrBlank() || !user.login.isNullOrBlank())) {
                                viewChannel(id = user.id, login = user.login, name = user.display_name, channelLogo = user.channelLogo)
                            }
                        }
                    }
                }
            }
        }
    }

// Navigation listeners

    override fun startStream(stream: Stream) {
        fragNavController.pushFragment(ChannelPagerFragment.newInstance(stream.user_id, stream.user_login, stream.user_name, stream.channelLogo))
    }

    override fun viewChannel(id: String?, login: String?, name: String?, channelLogo: String?, updateLocal: Boolean) {
        fragNavController.pushFragment(ChannelPagerFragment.newInstance(id, login, name, channelLogo, updateLocal))
    }

    fun popFragment() {
        fragNavController.popFragment()
    }

    fun openSearch() {
        fragNavController.pushFragment(SearchFragment())
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
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
                override fun onFragmentTransaction(fragment: Fragment?, transactionType: FragNavController.TransactionType) {
                }

                override fun onTabTransaction(fragment: Fragment?, index: Int) {
                }
            }
        }
    }
}
