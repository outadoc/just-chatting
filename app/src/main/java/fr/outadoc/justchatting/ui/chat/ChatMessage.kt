package fr.outadoc.justchatting.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.ui.common.ensureColorIsAccessible
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.util.isLight
import fr.outadoc.justchatting.util.isOdd
import org.koin.androidx.compose.get

@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    chatPreferencesRepository: ChatPreferencesRepository = get(),
    entries: List<ChatEntry>
) {
    val listState = rememberLazyListState()
    LaunchedEffect(entries) {
        listState.scrollToItem(
            index = (entries.size - 1).coerceAtLeast(0)
        )
    }

    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        itemsIndexed(
            items = entries,
            contentType = { _, item ->
                when (item) {
                    is ChatEntry.Highlighted -> 1
                    is ChatEntry.Simple -> 2
                }
            }
        ) { index, item ->
            if (index.isOdd) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    ChatMessage(
                        modifier = Modifier.fillMaxWidth(),
                        message = item
                    )
                }
            } else {
                ChatMessage(
                    modifier = Modifier.fillMaxWidth(),
                    message = item
                )
            }
        }
    }
}

@Composable
fun ChatMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry
) {
    when (message) {
        is ChatEntry.Highlighted -> {
            HighlightedMessage(
                modifier = modifier,
                message = message
            )
        }
        is ChatEntry.Simple -> {
            SimpleMessage(
                modifier = modifier,
                message = message
            )
        }
    }
}

@Composable
fun HighlightedMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry.Highlighted
) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        shape = RectangleShape
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .width(4.dp)
                    .fillMaxHeight()
            )

            Column {
                message.header?.let { header ->
                    Row(
                        modifier = Modifier.padding(
                            start = 4.dp,
                            end = 4.dp,
                            top = 4.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        message.headerIconResId?.let { resId ->
                            Icon(
                                modifier = Modifier.padding(end = 4.dp),
                                painter = painterResource(id = resId),
                                contentDescription = null
                            )
                        }

                        Text(
                            text = header,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                message.data?.let { data ->
                    ChatMessageData(
                        modifier = modifier.padding(
                            start = 4.dp,
                            end = 4.dp,
                            bottom = 4.dp
                        ),
                        data = data
                    )
                }

                if (message.header != null && message.data == null) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun SimpleMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry.Simple
) {
    Row {
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
        )

        ChatMessageData(
            modifier = modifier.padding(4.dp),
            data = message.data
        )
    }
}

@Composable
fun ChatMessageData(
    modifier: Modifier = Modifier,
    data: ChatEntry.Data
) {
    Text(
        modifier = modifier,
        text = data.toAnnotatedString(animateEmotes = true),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ChatEntry.Data.toAnnotatedString(
    animateEmotes: Boolean
): AnnotatedString {
    val screenDensity = LocalDensity.current.density
    val isDarkMode = MaterialTheme.colorScheme.isLight

    val randomChatColors = integerArrayResource(id = R.array.randomChatColors)

    val color = color
        ?.let { color ->
            ensureColorIsAccessible(
                foreground = android.graphics.Color.parseColor(color),
                background = MaterialTheme.colorScheme.surface.toArgb()
            )
        }
        ?: randomChatColors.random()

    return buildAnnotatedString {
        if (userName != null) {
            withStyle(
                SpanStyle(
                    color = Color(color),
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(userName)
                append(if (isAction) " " else ": ")
            }
        }

        if (message != null) {
            append(message)
        }
    }
}
