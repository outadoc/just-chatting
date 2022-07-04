package com.github.andreyasadchy.xtra.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.ui.main.MainViewModel
import com.github.andreyasadchy.xtra.util.isNetworkAvailable
import javax.inject.Inject

abstract class BaseNetworkFragment : Fragment(), Injectable {

    private companion object {
        const val LAST_KEY = "last"
        const val RESTORE_KEY = "restore"
        const val CREATED_KEY = "created"
    }

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mainViewModel by activityViewModels<MainViewModel> { viewModelFactory }

    private var enableNetworkCheck = true
    private var lastState = false
    private var shouldRestore = false
    private var isInitialized = false
    private var created = false

    abstract fun initialize()
    abstract fun onNetworkRestored()
    open fun onNetworkLost() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (enableNetworkCheck) {
            lastState = savedInstanceState?.getBoolean(LAST_KEY)
                ?: requireContext().isNetworkAvailable
            shouldRestore = savedInstanceState?.getBoolean(RESTORE_KEY) ?: false
            created = savedInstanceState?.getBoolean(CREATED_KEY) ?: false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!enableNetworkCheck) {
            initialize()
            return
        }

        if (!isInitialized && (created || (lastState && userVisibleHint))) {
            init()
        }

        mainViewModel.isNetworkAvailable.observe(viewLifecycleOwner) { event ->
            val isOnline = event.peekContent()
            if (isOnline) {
                if (!lastState) {
                    if (isInitialized) {
                        onNetworkRestored()
                    } else {
                        init()
                    }
                    shouldRestore = false
                }
            } else if (isInitialized) {
                onNetworkLost()
            }

            lastState = isOnline
        }
    }

    override fun onResume() {
        super.onResume()
        if (!enableNetworkCheck) return

        if (!isInitialized) {
            if (isResumed && lastState) {
                init()
            }
        } else if (shouldRestore && lastState) {
            onNetworkRestored()
            shouldRestore = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (enableNetworkCheck) {
            isInitialized = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (enableNetworkCheck) {
            outState.putBoolean(LAST_KEY, lastState)
            outState.putBoolean(RESTORE_KEY, shouldRestore)
            outState.putBoolean(CREATED_KEY, created)
        }
    }

    private fun init() {
        initialize()
        isInitialized = true
        created = true
    }
}
