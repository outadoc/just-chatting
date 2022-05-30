package com.github.andreyasadchy.xtra.ui.settings

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.preference.*
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.ui.Utils
import com.github.andreyasadchy.xtra.ui.settings.api.DragListFragment
import com.github.andreyasadchy.xtra.util.*
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_settings.*
import javax.inject.Inject

class SettingsActivity : AppCompatActivity(), HasAndroidInjector, Injectable {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    var recreate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_settings)
        toolbar.navigationIcon = Utils.getNavigationIcon(this)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        recreate = savedInstanceState?.getBoolean(SettingsFragment.KEY_CHANGED) == true
        if (savedInstanceState == null || recreate) {
            recreate = false
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SettingsFragment.KEY_CHANGED, recreate)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    class SettingsFragment : PreferenceFragmentCompat(), Injectable {

        @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
        private val viewModel by viewModels<SettingsViewModel> { viewModelFactory }

        private var changed = false

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            changed = savedInstanceState?.getBoolean(KEY_CHANGED) == true
            if (changed) {
                requireActivity().setResult(Activity.RESULT_OK)
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val activity = requireActivity()
            val changeListener = Preference.OnPreferenceChangeListener { _, _ ->
                setResult()
                true
            }

            findPreference<ListPreference>(C.UI_LANGUAGE)?.setOnPreferenceChangeListener { _, _ ->
                (activity as? SettingsActivity)?.recreate = true
                changed = true
                activity.recreate()
                true
            }

            findPreference<ListPreference>(C.UI_CUTOUTMODE)?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setOnPreferenceChangeListener { _, _ ->
                        changed = true
                        activity.recreate()
                        true
                    }
                } else {
                    isVisible = false
                }
            }

            findPreference<Preference>("theme_settings")?.setOnPreferenceClickListener {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, ThemeSettingsFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }

            findPreference<SwitchPreferenceCompat>(C.UI_ROUNDUSERIMAGE)?.onPreferenceChangeListener = changeListener
            findPreference<SwitchPreferenceCompat>(C.UI_TRUNCATEVIEWCOUNT)?.onPreferenceChangeListener = changeListener
            findPreference<SwitchPreferenceCompat>(C.UI_UPTIME)?.onPreferenceChangeListener = changeListener
            findPreference<SwitchPreferenceCompat>(C.UI_TAGS)?.onPreferenceChangeListener = changeListener
            findPreference<SwitchPreferenceCompat>(C.UI_BROADCASTERSCOUNT)?.onPreferenceChangeListener = changeListener
            findPreference<SwitchPreferenceCompat>(C.UI_BOOKMARK_TIME_LEFT)?.onPreferenceChangeListener = changeListener
            findPreference<SwitchPreferenceCompat>(C.UI_SCROLLTOP)?.onPreferenceChangeListener = changeListener
            findPreference<ListPreference>(C.PORTRAIT_COLUMN_COUNT)?.onPreferenceChangeListener = changeListener
            findPreference<ListPreference>(C.LANDSCAPE_COLUMN_COUNT)?.onPreferenceChangeListener = changeListener
            findPreference<SwitchPreferenceCompat>(C.COMPACT_STREAMS)?.onPreferenceChangeListener = changeListener

            findPreference<SeekBarPreference>("chatWidth")?.apply {
                summary = context.getString(R.string.pixels, activity.prefs().getInt(C.LANDSCAPE_CHAT_WIDTH, 30))
                setOnPreferenceChangeListener { _, newValue ->
                    setResult()
                    val chatWidth = DisplayUtils.calculateLandscapeWidthByPercent(activity, newValue as Int)
                    summary = context.getString(R.string.pixels, chatWidth)
                    activity.prefs().edit { putInt(C.LANDSCAPE_CHAT_WIDTH, chatWidth) }
                    true
                }
            }

            findPreference<Preference>("player_button_settings")?.setOnPreferenceClickListener {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, PlayerButtonSettingsFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }

            findPreference<Preference>("player_menu_settings")?.setOnPreferenceClickListener {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, PlayerMenuSettingsFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                findPreference<ListPreference>(C.PLAYER_BACKGROUND_PLAYBACK)?.apply {
                    setEntries(R.array.backgroundPlaybackNoPipEntries)
                    setEntryValues(R.array.backgroundPlaybackNoPipValues)
                }
            }

            findPreference<Preference>("buffer_settings")?.setOnPreferenceClickListener {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, BufferSettingsFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }

            findPreference<Preference>("clear_video_positions")?.setOnPreferenceChangeListener { _, _ ->
                viewModel.deletePositions()
                requireContext().shortToast(R.string.cleared)
                true
            }

            findPreference<Preference>("token_settings")?.setOnPreferenceClickListener {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, TokenSettingsFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }

            findPreference<Preference>("api_settings")?.setOnPreferenceClickListener {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, DragListFragment.newInstance())
                    .addToBackStack(null)
                    .commit()
                true
            }

            findPreference<Preference>("admin_settings")?.setOnPreferenceClickListener {
                startActivity(Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")))
                true
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            outState.putBoolean(KEY_CHANGED, changed)
            super.onSaveInstanceState(outState)
        }

        private fun setResult() {
            if (!changed) {
                changed = true
                requireActivity().setResult(Activity.RESULT_OK)
            }
        }

        companion object {
            const val KEY_CHANGED = "changed"
        }
    }

    class ThemeSettingsFragment : PreferenceFragmentCompat() {

        private var changed = false

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            changed = savedInstanceState?.getBoolean(SettingsFragment.KEY_CHANGED) == true
            if (changed) {
                requireActivity().setResult(Activity.RESULT_OK)
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.theme_preferences, rootKey)
            val activity = requireActivity()

            findPreference<ListPreference>(C.THEME)?.setOnPreferenceChangeListener { _, _ ->
                changed = true
                activity.recreate()
                true
            }
            findPreference<SwitchPreferenceCompat>(C.UI_THEME_FOLLOW_SYSTEM)?.setOnPreferenceChangeListener { _, _ ->
                changed = true
                activity.recreate()
                true
            }
            findPreference<ListPreference>(C.UI_THEME_DARK_ON)?.setOnPreferenceChangeListener { _, _ ->
                changed = true
                activity.recreate()
                true
            }
            findPreference<ListPreference>(C.UI_THEME_DARK_OFF)?.setOnPreferenceChangeListener { _, _ ->
                changed = true
                activity.recreate()
                true
            }
            findPreference<SwitchPreferenceCompat>(C.UI_STATUSBAR)?.setOnPreferenceChangeListener { _, _ ->
                changed = true
                activity.recreate()
                true
            }
            findPreference<SwitchPreferenceCompat>(C.UI_NAVBAR)?.setOnPreferenceChangeListener { _, _ ->
                changed = true
                activity.recreate()
                true
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            outState.putBoolean(SettingsFragment.KEY_CHANGED, changed)
            super.onSaveInstanceState(outState)
        }
    }

    class PlayerButtonSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.player_button_preferences, rootKey)
        }
    }

    class PlayerMenuSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.player_menu_preferences, rootKey)
        }
    }

    class BufferSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.buffer_preferences, rootKey)
        }
    }

    class TokenSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.token_preferences, rootKey)
        }
    }
}