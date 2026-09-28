package fr.outadoc.justchatting.feature.timeline.presentation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.shared.presentation.ui.StreamTagChip
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.tags_more_cd
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.pluralStringResource

/**
 * Shows as many [tags] as fit on a single line, followed by a "+N" counter for the tags
 * that didn't fit.
 */
@Composable
internal fun SingleLineTagList(
    modifier: Modifier = Modifier,
    tags: ImmutableList<String>,
    spacing: Dp = 6.dp,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val maxWidth = constraints.maxWidth
        val spacingPx = spacing.roundToPx()
        val looseConstraints = Constraints(maxWidth = maxWidth)

        val chips: List<Placeable> =
            tags.mapIndexed { index, tag ->
                subcompose("tag-$index") {
                    StreamTagChip(
                        tag = tag,
                        isCompact = true,
                    )
                }.first().measure(looseConstraints)
            }

        // A slot can only be subcomposed once per measure pass, so cache the counters.
        val counters = mutableMapOf<Int, Placeable>()

        fun measureCounter(hiddenCount: Int): Placeable =
            counters.getOrPut(hiddenCount) {
                subcompose("more-$hiddenCount") {
                    MoreTagsCounter(hiddenCount = hiddenCount)
                }.first().measure(looseConstraints)
            }

        // Keep as many tags as possible, while reserving room for the counter if some tags
        // still need to be hidden after the current one.
        var usedWidth = 0
        var shownCount = 0
        for ((index, chip) in chips.withIndex()) {
            val chipWidth = chip.width + if (shownCount > 0) spacingPx else 0
            val hiddenAfter = chips.size - index - 1
            val reservedWidth =
                if (hiddenAfter > 0) {
                    measureCounter(hiddenAfter).width + spacingPx
                } else {
                    0
                }

            if (usedWidth + chipWidth + reservedWidth > maxWidth) break

            usedWidth += chipWidth
            shownCount++
        }

        val shownChips = chips.take(shownCount)
        val hiddenCount = chips.size - shownCount
        val counter: Placeable? = if (hiddenCount > 0) measureCounter(hiddenCount) else null

        val height =
            (shownChips.map { it.height } + listOfNotNull(counter?.height))
                .maxOrNull() ?: 0

        val width =
            if (counter != null) {
                usedWidth + (if (shownCount > 0) spacingPx else 0) + counter.width
            } else {
                usedWidth
            }

        layout(width.coerceAtMost(maxWidth), height) {
            var x = 0
            shownChips.forEach { chip ->
                chip.placeRelative(x, (height - chip.height) / 2)
                x += chip.width + spacingPx
            }

            counter?.placeRelative(x, (height - counter.height) / 2)
        }
    }
}

@Composable
private fun MoreTagsCounter(
    modifier: Modifier = Modifier,
    hiddenCount: Int,
) {
    val description = pluralStringResource(Res.plurals.tags_more_cd, hiddenCount, hiddenCount.toString())

    Text(
        modifier =
            modifier.semantics {
                contentDescription = description
            },
        text = "+$hiddenCount",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}
