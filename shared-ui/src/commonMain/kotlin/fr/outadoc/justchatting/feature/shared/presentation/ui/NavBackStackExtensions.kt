package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import fr.outadoc.justchatting.feature.shared.presentation.DefaultScreen
import fr.outadoc.justchatting.feature.shared.presentation.DetailScreen
import fr.outadoc.justchatting.feature.shared.presentation.Screen

/**
 * Replaces any currently-open [DetailScreen] entry with [detail], so that at most one
 * detail entry is ever on the back stack at a time.
 */
internal fun NavBackStack<NavKey>.navigateToDetail(detail: DetailScreen) {
    removeAll { it is DetailScreen }
    add(detail)
}

/**
 * Opens the settings on top of the current tab, so that navigating up returns to it.
 */
internal fun NavBackStack<NavKey>.openSettings() {
    removeAll { it is DetailScreen || it is Screen.Settings }
    add(Screen.Settings)
}

/**
 * Closes the settings (and any of its open sections), falling back to the default tab if
 * the settings were the only thing on the back stack.
 */
internal fun NavBackStack<NavKey>.closeSettings() {
    removeAll { it is DetailScreen || it is Screen.Settings }
    if (isEmpty()) {
        add(DefaultScreen)
    }
}
