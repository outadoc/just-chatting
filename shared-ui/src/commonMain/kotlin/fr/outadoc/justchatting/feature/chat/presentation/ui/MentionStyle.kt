package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.isSpecified
import kotlin.math.max
import kotlin.math.min

/**
 * Tag of the string annotation marking a mention of the app user,
 * which [drawMentionPills] draws a tonal pill behind.
 */
private const val AppUserMentionTag = "app_user_mention"

/**
 * Non-breaking space added on each side of a highlighted mention, to reserve room for
 * the pill's horizontal padding without letting the line wrap inside of it.
 */
private const val MentionPillPadding = '\u00A0'

/**
 * Appends a mention to the string. Mentions of the app user are padded and annotated
 * so that [drawMentionPills] can draw a rounded background behind them.
 */
internal fun AnnotatedString.Builder.appendMention(
    text: String,
    mentioned: Boolean,
    mentionColor: Color,
) {
    if (mentioned) {
        pushStringAnnotation(tag = AppUserMentionTag, annotation = text)
        append(MentionPillPadding)
    }

    withStyle(
        SpanStyle(
            fontWeight = FontWeight.Bold,
            color = if (mentioned) mentionColor else Color.Unspecified,
        ),
    ) {
        append(text)
    }

    if (mentioned) {
        append(MentionPillPadding)
        pop()
    }
}

/**
 * Draws a rounded pill of [pillColor] behind every mention of the app user appended
 * by [appendMention]. Must be applied to the [androidx.compose.material3.Text] whose
 * latest layout [layoutResult] returns.
 */
internal fun Modifier.drawMentionPills(
    layoutResult: () -> TextLayoutResult?,
    pillColor: Color,
): Modifier =
    drawBehind {
        val layout = layoutResult() ?: return@drawBehind
        val text = layout.layoutInput.text
        val mentions = text.getStringAnnotations(AppUserMentionTag, 0, text.length)
        if (mentions.isEmpty()) return@drawBehind

        // Don't draw pills for text cut off by maxLines
        val visibleEnd = layout.getLineEnd(layout.lineCount - 1, visibleEnd = true)
        val fontSize = layout.layoutInput.style.fontSize
        val fontSizePx = if (fontSize.isSpecified) fontSize.toPx() else null

        fun drawPill(
            line: Int,
            left: Float,
            right: Float,
        ) {
            // The line height can be much taller than the text (to fit emotes),
            // so size the pill from the font size and center it on the glyphs.
            val baseline = layout.getLineBaseline(line)
            val top = fontSizePx?.let { baseline - it * 1.05f } ?: layout.getLineTop(line)
            val bottom = fontSizePx?.let { baseline + it * 0.35f } ?: layout.getLineBottom(line)
            val height = bottom - top

            drawRoundRect(
                color = pillColor,
                topLeft = Offset(x = left, y = top),
                size = Size(width = right - left, height = height),
                cornerRadius = CornerRadius(height / 2),
            )
        }

        mentions.forEach { range ->
            val end = min(range.end, visibleEnd)
            if (range.start >= end) return@forEach

            // Split the mention into one segment per line it spans
            val segments = mutableListOf<MentionSegment>()
            for (offset in range.start until end) {
                val line = layout.getLineForOffset(offset)
                val box = layout.getBoundingBox(offset)
                val last = segments.lastOrNull()

                if (last?.line == line) {
                    segments[segments.lastIndex] =
                        last.copy(
                            left = min(last.left, box.left),
                            right = max(last.right, box.right),
                        )
                } else {
                    segments += MentionSegment(line = line, left = box.left, right = box.right)
                }
            }

            // Where the mention wraps, there's no padding character left to reserve
            // space for the pill, so extend it outwards by the same amount instead.
            val paddingPx = layout.getBoundingBox(range.start).width

            segments.forEachIndexed { index, segment ->
                drawPill(
                    line = segment.line,
                    left = if (index > 0) segment.left - paddingPx else segment.left,
                    right = if (index < segments.lastIndex) segment.right + paddingPx else segment.right,
                )
            }
        }
    }

private data class MentionSegment(
    val line: Int,
    val left: Float,
    val right: Float,
)
