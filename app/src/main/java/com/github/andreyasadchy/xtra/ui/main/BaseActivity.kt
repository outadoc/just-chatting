package com.github.andreyasadchy.xtra.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

open class BaseActivity : AppCompatActivity(), HasAndroidInjector, Injectable {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        prefs = prefs()

        if (prefs.getBoolean(C.FIRST_LAUNCH2, true)) {
            PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
            PreferenceManager.setDefaultValues(this, R.xml.api_preferences, true)
            prefs.edit {
                putBoolean(C.FIRST_LAUNCH2, false)
            }
        }

        if (prefs.getBoolean(C.FIRST_LAUNCH, true)) {
            prefs.edit {
                putBoolean(C.FIRST_LAUNCH, false)
            }
        }

        if (prefs.getBoolean(C.FIRST_LAUNCH1, true)) {
            prefs.edit {
                putBoolean(C.FIRST_LAUNCH1, false)
            }
        }
    }
}
