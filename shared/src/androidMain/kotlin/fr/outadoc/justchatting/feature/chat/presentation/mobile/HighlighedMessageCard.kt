package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent.Message.Highlighted.Level
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.HighlightLevelPreviewProvider
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.isDark

@Composable
internal fun HighlightedMessageCard(
    modifier: Modifier = Modifier,
    level: Level = Level.Base,
    content: @Composable () -> Unit,
) {
    val color = when (level) {
        Level.Base -> null
        Level.One -> Color(0xff6b816e)
        Level.Two -> Color(0xff32843b)
        Level.Three -> Color(0xff007a6c)
        Level.Four -> Color(0xff0080a9)
        Level.Five -> Color(0xff0070db)
        Level.Six -> Color(0xff016cd9)
        Level.Seven -> Color(0xff731acb)
        Level.Eight -> Color(0xffbe0bb7)
        Level.Nine -> Color(0xffab2078)
        Level.Ten -> Color(0xffc90216)
    }

    MaterialTheme(colorScheme = singleSourceColorScheme(color)) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .background(color ?: MaterialTheme.colorScheme.primary)
                    .width(4.dp)
                    .fillMaxHeight(),
            )

            Card(
                modifier = modifier.padding(vertical = 4.dp),
                shape = RectangleShape,
            ) {
                content()
            }
        }
    }
}

@ThemePreviews
@Composable
internal fun HighlightLevelPreview(
    @PreviewParameter(HighlightLevelPreviewProvider::class) level: Level,
) {
    AppTheme {
        HighlightedMessageCard(level = level) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = "This is a highlighted message",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Stable
@Composable
internal fun singleSourceColorScheme(
    color: Color?,
    parentScheme: ColorScheme = MaterialTheme.colorScheme,
): ColorScheme {
    return color?.let { currentSourceColor ->
        if (parentScheme.isDark) {
            darkSchemeFromColor(currentSourceColor)
        } else {
            lightSchemeFromColor(currentSourceColor)
        }
    } ?: parentScheme
}
