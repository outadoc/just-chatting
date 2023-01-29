package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.feature.chat.presentation.ChatEntry
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun HighlightedMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry.Highlighted,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    animateEmotes: Boolean,
    appUser: AppUser,
    backgroundHint: Color = MaterialTheme.colorScheme.surface,
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .background(MaterialTheme.colorScheme.primary)
                .width(4.dp)
                .fillMaxHeight(),
        )

        Column {
            Card(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth(),
                shape = RectangleShape,
            ) {
                message.header?.let { header ->
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        message.headerIconResId?.let { resId ->
                            Icon(
                                modifier = Modifier.padding(end = 4.dp),
                                painter = painterResource(id = resId),
                                contentDescription = null,
                            )
                        }

                        Text(
                            text = header,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }

            message.data?.let { data ->
                ChatMessageData(
                    modifier = modifier.padding(4.dp),
                    data = data,
                    inlineContent = inlineContent,
                    animateEmotes = animateEmotes,
                    appUser = appUser,
                    backgroundHint = backgroundHint,
                )
            }
        }
    }
}
