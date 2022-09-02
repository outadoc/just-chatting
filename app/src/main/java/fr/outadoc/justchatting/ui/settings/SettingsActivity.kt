package fr.outadoc.justchatting.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewHolder: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewHolder = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(viewHolder.root)

        viewHolder.toolbar.setNavigationIcon(R.drawable.ic_back)
        viewHolder.toolbar.setNavigationOnClickListener { onBackPressed() }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore =
                SettingsDataStore(requireContext().applicationContext)

            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}
