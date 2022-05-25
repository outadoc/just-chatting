package com.github.andreyasadchy.xtra.ui.main

import android.app.ActivityManager
import android.app.PictureInPictureParams
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.ui.channel.ChannelPagerFragment
import com.github.andreyasadchy.xtra.ui.chat.ChatFragment
import com.github.andreyasadchy.xtra.ui.clips.BaseClipsFragment
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.common.pagers.MediaPagerFragment
import com.github.andreyasadchy.xtra.ui.download.HasDownloadDialog
import com.github.andreyasadchy.xtra.ui.follow.FollowMediaFragment
import com.github.andreyasadchy.xtra.ui.games.GameFragment
import com.github.andreyasadchy.xtra.ui.games.GamesFragment
import com.github.andreyasadchy.xtra.ui.player.BasePlayerFragment
import com.github.andreyasadchy.xtra.ui.player.clip.ClipPlayerFragment
import com.github.andreyasadchy.xtra.ui.player.offline.OfflinePlayerFragment
import com.github.andreyasadchy.xtra.ui.player.stream.StreamPlayerFragment
import com.github.andreyasadchy.xtra.ui.player.video.VideoPlayerFragment
import com.github.andreyasadchy.xtra.ui.saved.SavedMediaFragment
import com.github.andreyasadchy.xtra.ui.saved.downloads.DownloadsFragment
import com.github.andreyasadchy.xtra.ui.search.SearchFragment
import com.github.andreyasadchy.xtra.ui.search.tags.BaseTagSearchFragment
import com.github.andreyasadchy.xtra.ui.streams.BaseStreamsFragment
import com.github.andreyasadchy.xtra.ui.top.TopFragment
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosFragment
import com.github.andreyasadchy.xtra.ui.view.SlidingLayout
import com.github.andreyasadchy.xtra.util.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncapdevi.fragnav.FragNavController
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_media_pager.view.*
import java.util.*
import javax.inject.Inject


const val INDEX_GAMES = FragNavController.TAB1
const val INDEX_TOP = FragNavController.TAB2
const val INDEX_FOLLOWED = FragNavController.TAB3
const val INDEX_DOWNLOADS = FragNavController.TAB4

class MainActivity : AppCompatActivity(), GamesFragment.OnGameSelectedListener, GamesFragment.OnTagGames, BaseStreamsFragment.OnStreamSelectedListener, OnChannelSelectedListener, BaseClipsFragment.OnClipSelectedListener, BaseVideosFragment.OnVideoSelectedListener, HasAndroidInjector, DownloadsFragment.OnVideoSelectedListener, Injectable, SlidingLayout.Listener {

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
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.setNetworkAvailable(isNetworkAvailable)
        }
    }
    private lateinit var prefs: SharedPreferences

    //Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = prefs()
        val lang = prefs.getString(C.UI_LANGUAGE, "auto") ?: "auto"
        if (lang != "auto") {
            val config = resources.configuration
            val locale = Locale(lang)
            Locale.setDefault(locale)
            config.setLocale(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                createConfigurationContext(config)
            }
            resources.updateConfiguration(config, resources.displayMetrics)
            application.resources.updateConfiguration(config, resources.displayMetrics)
        }
        if (prefs.getBoolean(C.FIRST_LAUNCH2, true)) {
            PreferenceManager.setDefaultValues(this@MainActivity, R.xml.root_preferences, false)
            PreferenceManager.setDefaultValues(this@MainActivity, R.xml.player_button_preferences, true)
            PreferenceManager.setDefaultValues(this@MainActivity, R.xml.player_menu_preferences, true)
            PreferenceManager.setDefaultValues(this@MainActivity, R.xml.buffer_preferences, true)
            PreferenceManager.setDefaultValues(this@MainActivity, R.xml.token_preferences, true)
            PreferenceManager.setDefaultValues(this@MainActivity, R.xml.api_preferences, true)
            prefs.edit {
                putBoolean(C.FIRST_LAUNCH2, false)
                putInt(C.LANDSCAPE_CHAT_WIDTH, DisplayUtils.calculateLandscapeWidthByPercent(this@MainActivity, 30))
                if (resources.getBoolean(R.bool.isTablet)) {
                    putString(C.PORTRAIT_COLUMN_COUNT, "2")
                    putString(C.LANDSCAPE_COLUMN_COUNT, "3")
                }
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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                    putString(C.PLAYER_BACKGROUND_PLAYBACK, "1")
                } else {
                    putString(C.PLAYER_BACKGROUND_PLAYBACK, "0")
                }
                putBoolean(C.FIRST_LAUNCH1, false)
            }
        }
        applyTheme()
        setContentView(R.layout.activity_main)

        val notInitialized = savedInstanceState == null
        initNavigation()
        if ((User.get(this) !is NotLoggedIn && (prefs.getString(C.UI_STARTONFOLLOWED, "1")?.toInt() ?: 1 < 2)) || (User.get(this) is NotLoggedIn && (prefs.getString(C.UI_STARTONFOLLOWED, "1")?.toInt() ?: 1 == 0))) {
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
        restorePlayerFragment()
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        restorePlayerFragment()
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
                if ((User.get(this) !is NotLoggedIn && (prefs.getString(C.UI_STARTONFOLLOWED, "1")?.toInt() ?: 1 < 2)) || (User.get(this) is NotLoggedIn && (prefs.getString(C.UI_STARTONFOLLOWED, "1")?.toInt() ?: 1 == 0))) {
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

    private fun isBackgroundRunning(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return false
        } else {
            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == packageName) {
                            //If your app is the process in foreground, then it's not in running in background
                            return false
                        }
                    }
                }
            }
            return true
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        playerFragment?.let {
            if (isBackgroundRunning() || it.enterPictureInPicture()) {
                it.setUserLeaveHint()
                if (prefs.getString(C.PLAYER_BACKGROUND_PLAYBACK, "0") == "0") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                        if (!it.enterPictureInPicture()) {
                            it.maximize()
                        }
                        // player dialog
                        (it.childFragmentManager.findFragmentByTag("closeOnPip") as? BottomSheetDialogFragment?)?.dismiss()
                        // player chat message dialog
                        (it.childFragmentManager.findFragmentById(R.id.chatFragmentContainer)?.childFragmentManager?.findFragmentByTag("closeOnPip") as? BottomSheetDialogFragment?)?.dismiss()
                        // channel chat message dialog
                        val fragment = fragNavController.currentFrag as? ChannelPagerFragment?
                        if (fragment != null) {
                            ((fragment.childFragmentManager.findFragmentByTag("android:switcher:" + fragment.view?.viewPager?.id + ":" + fragment.view?.viewPager?.currentItem) as? ChatFragment?)?.childFragmentManager?.findFragmentByTag("closeOnPip") as? BottomSheetDialogFragment?)?.dismiss()
                        }
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
                } else {
                    if (prefs.getString(C.PLAYER_BACKGROUND_PLAYBACK, "0") == "1" && !it.isPaused()) {
                        (it as? StreamPlayerFragment)?.startAudioOnly() ?: (it as? VideoPlayerFragment)?.startAudioOnly() ?: (it as? OfflinePlayerFragment)?.startAudioOnly()
                    }
                }
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val url = intent.data.toString()
            when {
                url.contains("twitch.tv/videos/") -> {
                    val id = url.substringAfter("twitch.tv/videos/").substringBefore("?")
                    val offset = url.substringAfter("?t=").nullIfEmpty()?.let { (TwitchApiHelper.getDuration(it)?.toDouble() ?: 0.0) * 1000.0 }
                    viewModel.loadVideo(id, prefs.getString(C.HELIX_CLIENT_ID, ""), prefs.getString(C.TOKEN, ""), prefs.getString(C.GQL_CLIENT_ID, ""))
                    viewModel.video.observe(this) { video ->
                        if (video != null && video.id.isNotBlank()) {
                            startVideo(video, offset)
                        }
                    }
                }
                url.contains("/clip/") -> {
                    val id = url.substringAfter("/clip/").substringBefore("?")
                    viewModel.loadClip(id, prefs.getString(C.HELIX_CLIENT_ID, ""), prefs.getString(C.TOKEN, ""), prefs.getString(C.GQL_CLIENT_ID, ""))
                    viewModel.clip.observe(this) { clip ->
                        if (clip != null && clip.id.isNotBlank()) {
                            startClip(clip)
                        }
                    }
                }
                url.contains("clips.twitch.tv/") -> {
                    val id = url.substringAfter("clips.twitch.tv/").substringBefore("?")
                    viewModel.loadClip(id, prefs.getString(C.HELIX_CLIENT_ID, ""), prefs.getString(C.TOKEN, ""), prefs.getString(C.GQL_CLIENT_ID, ""))
                    viewModel.clip.observe(this) { clip ->
                        if (clip != null && clip.id.isNotBlank()) {
                            startClip(clip)
                        }
                    }
                }
                url.contains("twitch.tv/directory/game/") -> {
                    val name = url.substringAfter("twitch.tv/directory/game/").substringBefore("/")
                    openGame(id = null, name = Uri.decode(name))
                }
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
        } else {
            when (intent?.getIntExtra(KEY_CODE, -1)) {
                INTENT_OPEN_DOWNLOADS_TAB -> navBar.selectedItemId = R.id.fragment_downloads
                INTENT_OPEN_DOWNLOADED_VIDEO -> startOfflineVideo(intent.getParcelableExtra(KEY_VIDEO)!!)
                INTENT_OPEN_PLAYER -> playerFragment!!.maximize() //TODO if was closed need to reopen
            }
        }
    }

//Navigation listeners

    override fun openTagGames(tags: List<String>?) {
        fragNavController.pushFragment(GamesFragment.newInstance(tags))
    }

    override fun openGame(id: String?, name: String?, tags: List<String>?, updateLocal: Boolean) {
        fragNavController.pushFragment(GameFragment.newInstance(id, name, tags, updateLocal))
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

    override fun viewChannel(id: String?, login: String?, name: String?, channelLogo: String?, updateLocal: Boolean) {
        fragNavController.pushFragment(ChannelPagerFragment.newInstance(id, login, name, channelLogo, updateLocal))
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
        if (viewModel.isPlayerOpened) {
            if (playerFragment == null) {
                playerFragment = supportFragmentManager.findFragmentById(R.id.playerContainer) as BasePlayerFragment?
            } else {
                if (playerFragment?.slidingLayout?.secondView?.isVisible == false && prefs.getString(C.PLAYER_BACKGROUND_PLAYBACK, "0") == "0") {
                    playerFragment?.maximize()
                }
            }
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

    fun openTagSearch(getGameTags: Boolean = false, gameId: String? = null, gameName: String? = null) {
        fragNavController.pushFragment(BaseTagSearchFragment.newInstance(getGameTags, gameId, gameName))
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    private fun initNavigation() {
        fragNavController.apply {
            rootFragments = listOf(
                GamesFragment(),
                TopFragment(),
                FollowMediaFragment.newInstance(prefs.getBoolean(C.UI_FOLLOWPAGER, true), prefs.getString(C.UI_FOLLOW_DEFAULT_PAGE, "0")?.toInt(), !User.get(this@MainActivity).gqlToken.isNullOrBlank()),
                SavedMediaFragment.newInstance(prefs.getBoolean(C.UI_SAVEDPAGER, true), prefs.getString(C.UI_SAVED_DEFAULT_PAGE, "0")?.toInt())
            )
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
                    else -> throw IllegalArgumentException()
                }
                fragNavController.switchTab(index)
                true
            }

            setOnNavigationItemReselectedListener {
                val currentFragment = fragNavController.currentFrag
                when (it.itemId) {
                    R.id.fragment_games -> {
                        if (currentFragment is GamesFragment && currentFragment.arguments?.getStringArray(C.TAGS).isNullOrEmpty()) {
                            currentFragment.scrollToTop()
                        } else {
                            fragNavController.clearStack()
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
