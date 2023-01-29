package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun InReplyToMessage(
    modifier: Modifier = Modifier,
    userName: String,
    message: String,
) {
    CompositionLocalProvider(
        LocalContentColor provides LocalContentColor.current.copy(alpha = 0.8f),
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(16.dp)
                    .alignByBaseline()
                    .padding(end = 4.dp, top = 1.dp),
                imageVector = Icons.Default.Reply,
                contentDescription = stringResource(R.string.chat_replyingTo),
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("@$userName")
                    }

                    append(": $message")
                },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
