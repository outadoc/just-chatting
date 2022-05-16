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

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartScreenCallback, HasAndroidInjector, Injectable {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_settings)
        toolbar.navigationIcon = Utils.getNavigationIcon(this)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragmentCompat, pref: PreferenceScreen): Boolean {
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.settings, SettingsSubScreenFragment().apply { arguments = bundleOf(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT to pref.key) }, null)
//                .addToBackStack(null)
//                .commit()
        return true
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
            findPreference<ListPreference>(C.PORTRAIT_COLUMN_COUNT)!!.onPreferenceChangeListener = changeListener
            findPreference<ListPreference>(C.LANDSCAPE_COLUMN_COUNT)!!.onPreferenceChangeListener = changeListener

            findPreference<SwitchPreferenceCompat>(C.UI_STATUSBAR)!!.setOnPreferenceChangeListener { _, _ ->
                changed = true
                activity.apply {
                    applyTheme()
                    recreate()
                }
                true
            }
            findPreference<SwitchPreferenceCompat>(C.UI_NAVBAR)!!.setOnPreferenceChangeListener { _, _ ->
                changed = true
                activity.apply {
                    applyTheme()
                    recreate()
                }
                true
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                findPreference<ListPreference>(C.PLAYER_BACKGROUND_PLAYBACK)!!.setEntries(R.array.backgroundPlaybackNoPipEntries)
                findPreference<ListPreference>(C.PLAYER_BACKGROUND_PLAYBACK)!!.setEntryValues(R.array.backgroundPlaybackNoPipValues)
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                findPreference<ListPreference>(C.UI_CUTOUTMODE)!!.isVisible = false
            } else {
                findPreference<ListPreference>(C.UI_CUTOUTMODE)!!.setOnPreferenceChangeListener { _, _ ->
                    changed = true
                    activity.apply {
                        applyTheme()
                        recreate()
                    }
                    true
                }
            }

            findPreference<ListPreference>(C.UI_LANGUAGE)!!.setOnPreferenceChangeListener { _, _ ->
                changed = true
                activity.apply { recreate() }
                true
            }

            findPreference<ListPreference>(C.THEME)!!.setOnPreferenceChangeListener { _, _ ->
                changed = true
                activity.apply {
                    applyTheme()
                    recreate()
                }
                true
            }

            findPreference<SeekBarPreference>("chatWidth")!!.summary = "width: " + activity.prefs().getInt(C.LANDSCAPE_CHAT_WIDTH, 30).toString()
            findPreference<SeekBarPreference>("chatWidth")!!.setOnPreferenceChangeListener { _, newValue ->
                setResult()
                val chatWidth = DisplayUtils.calculateLandscapeWidthByPercent(activity, newValue as Int)
                activity.prefs().edit { putInt(C.LANDSCAPE_CHAT_WIDTH, chatWidth) }
                findPreference<SeekBarPreference>("chatWidth")!!.summary = "width: " + activity.prefs().getInt(C.LANDSCAPE_CHAT_WIDTH, 30).toString()
                true
            }

            findPreference<Preference>("clear_video_positions")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                viewModel.deletePositions()
                requireContext().shortToast(R.string.cleared)
                true
            }

            findPreference<Preference>("player_button_settings")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, PlayerButtonSettingsFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }

            findPreference<Preference>("player_menu_settings")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, PlayerMenuSettingsFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }

            findPreference<Preference>("buffer_settings")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, BufferSettingsFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }

            findPreference<Preference>("token_settings")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, TokenSettingsFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }

            findPreference<Preference>("admin_settings")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")))
                true
            }

            findPreference<Preference>("api_settings")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, DragListFragment.newInstance())
                    .addToBackStack(null)
                    .commit()
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

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
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