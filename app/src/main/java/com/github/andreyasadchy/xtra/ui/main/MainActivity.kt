package com.github.andreyasadchy.xtra.ui.main

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.*
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.kraken.Channel
import com.github.andreyasadchy.xtra.model.kraken.clip.Clip
import com.github.andreyasadchy.xtra.model.kraken.game.Game
import com.github.andreyasadchy.xtra.model.kraken.stream.Stream
import com.github.andreyasadchy.xtra.model.kraken.video.Video
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.ui.channel.ChannelPagerFragment
import com.github.andreyasadchy.xtra.ui.chat.ChatFragment
import com.github.andreyasadchy.xtra.ui.clips.BaseClipsFragment
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.common.pagers.MediaPagerFragment
import com.github.andreyasadchy.xtra.ui.download.HasDownloadDialog
import com.github.andreyasadchy.xtra.ui.downloads.DownloadsFragment
import com.github.andreyasadchy.xtra.ui.follow.FollowValidationFragment
import com.github.andreyasadchy.xtra.ui.games.GameFragment
import com.github.andreyasadchy.xtra.ui.games.GamesFragment
import com.github.andreyasadchy.xtra.ui.menu.MenuFragment
import com.github.andreyasadchy.xtra.ui.player.BasePlayerFragment
import com.github.andreyasadchy.xtra.ui.player.clip.ClipPlayerFragment
import com.github.andreyasadchy.xtra.ui.player.offline.OfflinePlayerFragment
import com.github.andreyasadchy.xtra.ui.player.stream.StreamPlayerFragment
import com.github.andreyasadchy.xtra.ui.player.video.VideoPlayerFragment
import com.github.andreyasadchy.xtra.ui.search.SearchFragment
import com.github.andreyasadchy.xtra.ui.streams.BaseStreamsFragment
import com.github.andreyasadchy.xtra.ui.top.TopFragment
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosFragment
import com.github.andreyasadchy.xtra.ui.view.SlidingLayout
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.DisplayUtils
import com.github.andreyasadchy.xtra.util.applyTheme
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.isNetworkAvailable
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.shortToast
import com.github.andreyasadchy.xtra.util.toast
import com.github.andreyasadchy.xtra.util.visible
import com.ncapdevi.fragnav.FragNavController
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


const val INDEX_GAMES = FragNavController.TAB1
const val INDEX_TOP = FragNavController.TAB2
const val INDEX_FOLLOWED = FragNavController.TAB3
const val INDEX_DOWNLOADS = FragNavController.TAB4
const val INDEX_MENU = FragNavController.TAB5

class MainActivity : AppCompatActivity(), GamesFragment.OnGameSelectedListener, BaseStreamsFragment.OnStreamSelectedListener, OnChannelSelectedListener, BaseClipsFragment.OnClipSelectedListener, BaseVideosFragment.OnVideoSelectedListener, HasAndroidInjector, DownloadsFragment.OnVideoSelectedListener, Injectable, SlidingLayout.Listener {

    companion object {
        const val KEY_CODE = "code"
        const val KEY_VIDEO = "video"

        const val INTENT_OPEN_DOWNLOADS_TAB = 0
        const val INTENT_OPEN_DOWNLOADED_VIDEO = 1
        const val INTENT_OPEN_PLAYER = 2
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MainViewModel> { viewModelFactory }
    var playerFragment: BasePlayerFragment? = null
        private set
    private val fragNavController = FragNavController(supportFragmentManager, R.id.fragmentContainer)
    private lateinit var prefs: SharedPreferences

    //Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = prefs()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = if (prefs.getBoolean(C.IGNORE_NOTCH, true)) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
            }
        }
        super.onCreate(savedInstanceState)
        if (prefs.getBoolean(C.FIRST_LAUNCH1, true)) {
            PreferenceManager.setDefaultValues(this@MainActivity, R.xml.root_preferences, false)
            prefs.edit {
                putBoolean(C.FIRST_LAUNCH1, false)
                putInt(C.LANDSCAPE_CHAT_WIDTH, DisplayUtils.calculateLandscapeWidthByPercent(this@MainActivity, 30))
                if (resources.getBoolean(R.bool.isTablet)) {
                    putString(C.PORTRAIT_COLUMN_COUNT, "2")
                    putString(C.LANDSCAPE_COLUMN_COUNT, "3")
                }
            }
        }
        applyTheme()
        setContentView(R.layout.activity_main)

        val notInitialized = savedInstanceState == null
        initNavigation()
        if (User.get(this) !is NotLoggedIn && prefs.getBoolean(C.UI_STARTONFOLLOWED, false)) {
            fragNavController.initialize(INDEX_FOLLOWED, savedInstanceState)
            if (notInitialized) {
                navBar.selectedItemId = R.id.fragment_follow
            }
        } else {
            fragNavController.initialize(INDEX_TOP, savedInstanceState)
            if (notInitialized) {
                navBar.selectedItemId = R.id.fragment_top
            }
        }
        var flag = notInitialized && !isNetworkAvailable
        viewModel.isNetworkAvailable.observe(this, Observer {
            it.getContentIfNotHandled()?.let { online ->
                if (online) {
                    viewModel.validate(this)
                }
                if (flag) {
                    shortToast(if (online) R.string.connection_restored else R.string.no_connection)
                } else {
                    flag = true
                }
            }
        })
        restorePlayerFragment()
    }

    override fun onResume() {
        super.onResume()
        restorePlayerFragment()
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
            startActivity(Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) })
            overridePendingTransition(0, 0)
        }
        when (requestCode) {
            1 -> { //Was not logged in
                when (resultCode) {//Logged in
                    RESULT_OK -> restartActivity()
                }
            }
            2 -> restartActivity() //Was logged in
        }
    }

    override fun onBackPressed() {
        if (!viewModel.isPlayerMaximized) {
            if (fragNavController.isRootFragment) {
                if (User.get(this) !is NotLoggedIn) {
                    if (fragNavController.currentStackIndex != INDEX_FOLLOWED) {
                        navBar.selectedItemId = R.id.fragment_follow
                    } else {
                        super.onBackPressed()
                    }
                } else {
                    if (fragNavController.currentStackIndex != INDEX_TOP) {
                        navBar.selectedItemId = R.id.fragment_top
                    } else {
                        super.onBackPressed()
                    }
                }
            } else {
                val currentFrag = fragNavController.currentFrag
                if (currentFrag !is ChannelPagerFragment || (currentFrag.currentFragment.let { it !is ChatFragment || !it.hideEmotesMenu() })) {
                    fragNavController.popFragment()
                }
            }
        } else {
            playerFragment?.let {
                if (it is StreamPlayerFragment) {
                    if (!it.hideEmotesMenu()) {
                        it.minimize()
                    }
                } else {
                    it.minimize()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults.indexOf(PackageManager.PERMISSION_DENIED) == -1) {
                    val fragment = fragNavController.currentFrag
                    if (fragment is HasDownloadDialog) {
                        fragment.showDownloadDialog()
                    } else if (fragment is MediaPagerFragment && fragment.currentFragment is HasDownloadDialog) {
                        (fragment.currentFragment as HasDownloadDialog).showDownloadDialog()
                    }
                } else {
                    toast(R.string.permission_denied)
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        playerFragment.let {
            if (it != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && it.enterPictureInPicture()) {
                try {
                    val params = PictureInPictureParams.Builder()
                            .setSourceRectHint(Rect(0, 0, it.playerWidth, it.playerHeight))
//                            .setAspectRatio(Rational(it.playerWidth, it.playerHeight))
                            .build()
                    enterPictureInPictureMode(params)
                } catch (e: IllegalStateException) {
                    //device doesn't support PIP
                }
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.also {
            when (it.getIntExtra(KEY_CODE, -1)) {
                INTENT_OPEN_DOWNLOADS_TAB -> navBar.selectedItemId = R.id.fragment_downloads
                INTENT_OPEN_DOWNLOADED_VIDEO -> startOfflineVideo(it.getParcelableExtra(KEY_VIDEO))
                INTENT_OPEN_PLAYER -> playerFragment?.maximize() //TODO if was closed need to reopen
            }
        }
    }

//Navigation listeners

    override fun openGame(game: Game) {
        fragNavController.pushFragment(GameFragment.newInstance(game))
    }

    override fun startStream(stream: Stream) {
//        playerFragment?.play(stream)
        startPlayer(StreamPlayerFragment.newInstance(stream))
    }

    override fun startVideo(video: Video, offset: Double?) {
        startPlayer(VideoPlayerFragment.newInstance(video, offset))
    }

    override fun startClip(clip: Clip) {
        startPlayer(ClipPlayerFragment.newInstance(clip))
    }

    override fun startOfflineVideo(video: OfflineVideo) {
        startPlayer(OfflinePlayerFragment.newInstance(video))
    }

    override fun viewChannel(channel: Channel) {
        fragNavController.pushFragment(ChannelPagerFragment.newInstance(channel))
    }

//SlidingLayout.Listener

    override fun onMaximize() {
        viewModel.onMaximize()
    }

    override fun onMinimize() {
        viewModel.onMinimize()
    }

    override fun onClose() {
        closePlayer()
    }

//Player methods

    private fun startPlayer(fragment: BasePlayerFragment) {
//        if (playerFragment == null) {
        playerFragment = fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.playerContainer, fragment).commit()
        viewModel.onPlayerStarted()
    }

    fun closePlayer() {
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .remove(supportFragmentManager.findFragmentById(R.id.playerContainer)!!)
                .commit()
        playerFragment = null
        viewModel.onPlayerClosed()
    }

    private fun restorePlayerFragment() {
        if (viewModel.isPlayerOpened && playerFragment == null) {
            playerFragment = supportFragmentManager.findFragmentById(R.id.playerContainer) as BasePlayerFragment?
        }
    }

    private fun hideNavigationBar() {
        navBarContainer.gone()
    }

    private fun showNavigationBar() {
        navBarContainer.visible()
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
            rootFragments = listOf(GamesFragment(), TopFragment(), FollowValidationFragment(), DownloadsFragment(), MenuFragment())
            fragmentHideStrategy = FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH
            transactionListener = object : FragNavController.TransactionListener {
                override fun onFragmentTransaction(fragment: Fragment?, transactionType: FragNavController.TransactionType) {
                }

                override fun onTabTransaction(fragment: Fragment?, index: Int) {
                }
            }
        }
        navBar.apply {
            setOnNavigationItemSelectedListener {
                val index = when (it.itemId) {
                    R.id.fragment_games -> INDEX_GAMES
                    R.id.fragment_top -> INDEX_TOP
                    R.id.fragment_follow -> INDEX_FOLLOWED
                    R.id.fragment_downloads -> INDEX_DOWNLOADS
                    R.id.fragment_menu -> INDEX_MENU
                    else -> throw IllegalArgumentException()
                }
                fragNavController.switchTab(index)
                true
            }

            setOnNavigationItemReselectedListener {
                val currentFragment = fragNavController.currentFrag
                when (it.itemId) {
                    R.id.fragment_games -> {
                        when (currentFragment) {
                            is GamesFragment -> currentFragment.scrollToTop()
                            else -> fragNavController.clearStack()
                        }
                    }
                    else -> if (fragNavController.isRootFragment) {
                        if (currentFragment is Scrollable) {
                            currentFragment.scrollToTop()
                        }
                    } else {
                        fragNavController.clearStack()
                    }
                }
            }
        }
    }
}
