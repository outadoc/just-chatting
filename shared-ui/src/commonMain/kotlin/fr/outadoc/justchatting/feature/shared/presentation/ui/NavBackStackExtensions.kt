package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import fr.outadoc.justchatting.feature.shared.presentation.DetailScreen

/**
 * Replaces any currently-open [DetailScreen] entry with [detail], so that at most one
 * detail entry is ever on the back stack at a time.
 */
internal fun NavBackStack<NavKey>.navigateToDetail(detail: DetailScreen) {
    removeAll { it is DetailScreen }
    add(detail)
}
