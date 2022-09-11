package fr.outadoc.justchatting.ui.chat

import android.util.Patterns
import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.ui.common.ensureColorIsAccessible
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.ui.view.emotes.EmoteItem
import fr.outadoc.justchatting.util.isOdd
import org.koin.androidx.compose.get
import kotlin.random.Random

private val emotePlaceholder = Placeholder(
    width = 2.em,
    height = 2.em,
    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
)

private val urlRegex = Patterns.WEB_URL.toRegex()
private const val UrlAnnotationTag = "URL"

@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    chatPreferencesRepository: ChatPreferencesRepository = get(),
    entries: List<ChatEntry>,
    emotes: Map<String, Emote>,
    onMessageClick: (ChatEntry) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(entries) {
        listState.scrollToItem(
            index = (entries.size - 1).coerceAtLeast(0)
        )
    }

    val animateEmotes by chatPreferencesRepository.animateEmotes.collectAsState(initial = true)

    val inlineContent = remember(emotes) {
        emotes.mapValues { (_, emote) ->
            InlineTextContent(emotePlaceholder) {
                EmoteItem(
                    emote = emote,
                    animateEmotes = animateEmotes
                )
            }
        }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMessageClick(item) },
                        message = item,
                        inlineContent = inlineContent,
                        animateEmotes = animateEmotes
                    )
                }
            } else {
                ChatMessage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMessageClick(item) },
                    message = item,
                    inlineContent = inlineContent,
                    animateEmotes = animateEmotes
                )
            }
        }
    }
}

@Composable
fun ChatMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry,
    inlineContent: Map<String, InlineTextContent>,
    animateEmotes: Boolean
) {
    when (message) {
        is ChatEntry.Highlighted -> {
            HighlightedMessage(
                modifier = modifier,
                message = message,
                inlineContent = inlineContent,
                animateEmotes = animateEmotes
            )
        }
        is ChatEntry.Simple -> {
            SimpleMessage(
                modifier = modifier,
                message = message,
                inlineContent = inlineContent,
                animateEmotes = animateEmotes
            )
        }
    }
}

@Composable
fun HighlightedMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry.Highlighted,
    inlineContent: Map<String, InlineTextContent>,
    animateEmotes: Boolean
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
                        data = data,
                        inlineContent = inlineContent,
                        animateEmotes = animateEmotes
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
    message: ChatEntry.Simple,
    inlineContent: Map<String, InlineTextContent>,
    animateEmotes: Boolean
) {
    Row {
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
        )

        ChatMessageData(
            modifier = modifier.padding(4.dp),
            data = message.data,
            inlineContent = inlineContent,
            animateEmotes = animateEmotes
        )
    }
}

@Composable
fun ChatMessageData(
    modifier: Modifier = Modifier,
    data: ChatEntry.Data,
    inlineContent: Map<String, InlineTextContent>,
    animateEmotes: Boolean
) {
    val inlineContent = inlineContent + data.emotes.orEmpty()
        .associate { emote ->
            Pair(
                emote.name,
                InlineTextContent(emotePlaceholder) {
                    EmoteItem(
                        emote = emote,
                        animateEmotes = animateEmotes
                    )
                }
            )
        }

    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val annotatedString = data.toAnnotatedString(inlineContent)

    val uriHandler = LocalUriHandler.current

    Text(
        modifier = modifier
            .pointerInput(data) {
                detectTapGestures { offset ->
                    layoutResult.value?.let {
                        val position = it.getOffsetForPosition(offset)
                        annotatedString.getStringAnnotations(position, position).firstOrNull()
                            ?.let { result ->
                                if (result.tag == UrlAnnotationTag) {
                                    uriHandler.openUri(result.item)
                                }
                            }
                    }
                }
            },
        onTextLayout = { layoutResult.value = it },
        text = annotatedString,
        inlineContent = inlineContent,
        style = MaterialTheme.typography.bodyMedium
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun ChatEntry.Data.toAnnotatedString(
    inlineContent: Map<String, InlineTextContent>
): AnnotatedString {
    val color = color
        ?.let { color ->
            ensureColorIsAccessible(
                foreground = android.graphics.Color.parseColor(color),
                background = MaterialTheme.colorScheme.surface.toArgb()
            )
        }
        ?: userName.getRandomChatColor()

    return buildAnnotatedString {
        withStyle(
            SpanStyle(
                color = Color(color),
                fontWeight = FontWeight.Bold
            )
        ) {
            append(userName)
            append(if (isAction) " " else ": ")
        }

        message?.split(' ')?.forEach { word ->
            when {
                word.matches(urlRegex) -> {
                    val url = if (word.startsWith("http")) word else "https://$word"
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        withAnnotation(tag = UrlAnnotationTag, annotation = url) {
                            append(word)
                        }
                    }
                }
                word.startsWith('@') -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(word)
                    }
                }
                word in inlineContent -> {
                    appendInlineContent(
                        id = word,
                        alternateText = word
                    )
                }
                else -> {
                    append(word)
                }
            }

            append(' ')
        }
    }
}

@Composable
@ColorInt
fun String.getRandomChatColor(): Int {
    val randomChatColors = integerArrayResource(id = R.array.randomChatColors)
    return randomChatColors.random(Random(hashCode()))
}
