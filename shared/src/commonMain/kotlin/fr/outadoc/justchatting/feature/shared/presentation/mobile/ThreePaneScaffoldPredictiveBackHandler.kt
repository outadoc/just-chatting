package fr.outadoc.justchatting.feature.shared.presentation.mobile

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldState
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.PredictiveBackHandler
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * An effect to add predictive back handling to a three pane scaffold.
 *
 * [NavigableListDetailPaneScaffold] and [NavigableSupportingPaneScaffold] apply this effect
 * automatically. If instead you are using [ListDetailPaneScaffold] or [SupportingPaneScaffold], use
 * the overloads that accept a [ThreePaneScaffoldState] and pass
 * [navigator.scaffoldState][ThreePaneScaffoldNavigator.scaffoldState] to the scaffold after adding
 * this effect to your composition.
 *
 * A predictive back gesture will cause the [navigator] to
 * [seekBack][ThreePaneScaffoldNavigator.seekBack] to the previous scaffold value. The progress can
 * be read from the [progressFraction][ThreePaneScaffoldState.progressFraction] of the navigator's
 * scaffold state. It will range from 0 (representing the start of the predictive back gesture) to
 * some fraction less than 1 (representing a "peek" or "preview" of the previous scaffold value). If
 * the gesture is committed, back navigation is performed. If the gesture is cancelled, the
 * navigator's scaffold state is reset.
 *
 * @param navigator The navigator instance to navigate through the scaffold.
 * @param backBehavior The back navigation behavior when the system back event happens. See
 *   [BackNavigationBehavior].
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Deprecated("Replace with compose-multiplatform implementation once made available")
@Composable
internal fun <T> ThreePaneScaffoldPredictiveBackHandler(
    navigator: ThreePaneScaffoldNavigator<T>,
    backBehavior: BackNavigationBehavior,
) {
    key(navigator, backBehavior) {
        PredictiveBackHandler(enabled = navigator.canNavigateBack(backBehavior)) { progress ->
            // code for gesture back started
            try {
                progress.collect { backEvent ->
                    navigator.seekBack(
                        backBehavior,
                        fraction =
                            backProgressToStateProgress(
                                progress = backEvent.progress,
                                scaffoldValue = navigator.scaffoldValue,
                            ),
                    )
                }
                // code for completion
                navigator.navigateBack(backBehavior)
            } catch (e: CancellationException) {
                // code for cancellation
                withContext(NonCancellable) { navigator.seekBack(backBehavior, fraction = 0f) }
            }
        }
    }
}

/**
 * Converts a progress value originating from a predictive back gesture into a progress value to
 * control a [ThreePaneScaffoldState].
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun backProgressToStateProgress(
    progress: Float,
    scaffoldValue: ThreePaneScaffoldValue,
): Float =
    ThreePaneScaffoldPredictiveBackEasing.transform(progress) *
        when (scaffoldValue.expandedCount) {
            1 -> SinglePaneProgressRatio
            2 -> DualPaneProgressRatio
            else -> TriplePaneProgressRatio
        }

private val ThreePaneScaffoldPredictiveBackEasing: Easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)
private const val SinglePaneProgressRatio: Float = 0.1f
private const val DualPaneProgressRatio: Float = 0.15f
private const val TriplePaneProgressRatio: Float = 0.2f

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private val ThreePaneScaffoldValue.expandedCount: Int
    get() {
        var count = 0
        if (primary == PaneAdaptedValue.Expanded) {
            count++
        }
        if (secondary == PaneAdaptedValue.Expanded) {
            count++
        }
        if (tertiary == PaneAdaptedValue.Expanded) {
            count++
        }
        return count
    }
