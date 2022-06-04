package com.github.andreyasadchy.xtra.ui.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
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

    protected var enableNetworkCheck = true
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (enableNetworkCheck) {
            if (!isInitialized && (created || (lastState && userVisibleHint))) {
                init()
            }
            mainViewModel.isNetworkAvailable.observe(
                viewLifecycleOwner,
                Observer {
                    val isOnline = it.peekContent()
                    if (isOnline) {
                        if (!lastState) {
                            shouldRestore = if (userVisibleHint) {
                                if (isInitialized) {
                                    onNetworkRestored()
                                } else {
                                    init()
                                }
                                false
                            } else {
                                true
                            }
                        }
                    } else {
                        if (isInitialized) {
                            onNetworkLost()
                        }
                    }
                    lastState = isOnline
                }
            )
        } else {
            initialize()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (enableNetworkCheck) {
            isInitialized = false
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (enableNetworkCheck) {
            if (!isInitialized) {
                if (isVisibleToUser && isResumed && lastState) {
                    init()
                }
            } else if (shouldRestore && lastState) {
                if (isVisibleToUser) {
                    onNetworkRestored()
                    shouldRestore = false
                }
            }
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
