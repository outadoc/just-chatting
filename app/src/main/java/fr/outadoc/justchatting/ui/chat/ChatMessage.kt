package fr.outadoc.justchatting.ui.chat

import android.util.Patterns
import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.chat.Badge
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.chat.TwitchBadge
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.ui.common.ensureColorIsAccessible
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.ui.view.emotes.BadgeItem
import fr.outadoc.justchatting.ui.view.emotes.EmoteItem
import fr.outadoc.justchatting.util.formatChannelUri
import fr.outadoc.justchatting.util.formatTimestamp
import fr.outadoc.justchatting.util.isOdd
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import kotlin.random.Random

private val emotePlaceholder = Placeholder(
    width = 2.em,
    height = 2.em,
    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
)

private val badgePlaceholder = Placeholder(
    width = 1.4.em,
    height = 1.4.em,
    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
)

private val urlRegex = Patterns.WEB_URL.toRegex()
private const val UrlAnnotationTag = "URL"

private val latinScriptUserName = "^\\w+$".toRegex()

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    chatPreferencesRepository: ChatPreferencesRepository = get(),
    onMessageClick: (ChatEntry) -> Unit
) {
    val animateEmotes by chatPreferencesRepository.animateEmotes.collectAsState(initial = true)
    val showTimestamps by chatPreferencesRepository.showTimestamps.collectAsState(initial = false)

    when (state) {
        ChatViewModel.State.Initial -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
        is ChatViewModel.State.Chatting -> {
            val scope = rememberCoroutineScope()
            val listState = rememberLazyListState()

            /*
            LaunchedEffect(state.chatMessages) {
                listState.scrollToItem(
                    index = (state.chatMessages.size - 1).coerceAtLeast(0)
                )
            }
             */

            Box(contentAlignment = Alignment.BottomCenter) {
                ChatList(
                    entries = state.chatMessages,
                    emotes = state.allEmotesMap,
                    badges = state.globalBadges + state.channelBadges,
                    animateEmotes = animateEmotes,
                    showTimestamps = showTimestamps,
                    listState = listState,
                    onMessageClick = onMessageClick
                )

                FloatingActionButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = {
                        scope.launch {
                            listState.scrollToItem(
                                index = (state.chatMessages.size - 1).coerceAtLeast(0)
                            )
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = "Scroll to bottom"
                    )
                }
            }
        }
    }
}

@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    entries: List<ChatEntry>,
    emotes: Map<String, Emote>,
    badges: List<TwitchBadge>,
    animateEmotes: Boolean,
    showTimestamps: Boolean,
    listState: LazyListState,
    onMessageClick: (ChatEntry) -> Unit
) {
    val inlinesEmotes = remember(emotes) {
        emotes.mapValues { (_, emote) ->
            InlineTextContent(emotePlaceholder) {
                EmoteItem(
                    emote = emote,
                    animateEmotes = animateEmotes
                )
            }
        }
    }

    val inlineBadges = remember(badges) {
        badges.associate { badge ->
            Pair(
                badge.inlineContentId,
                InlineTextContent(badgePlaceholder) {
                    BadgeItem(badge = badge)
                }
            )
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
            ChatMessage(
                modifier = Modifier
                    .background(
                        if (index.isOdd) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else Color.Transparent
                    )
                    .fillMaxWidth()
                    .clickable { onMessageClick(item) },
                message = item,
                inlineContent = inlinesEmotes + inlineBadges,
                animateEmotes = animateEmotes,
                showTimestamps = showTimestamps
            )
        }
    }
}

@Composable
fun ChatMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry,
    inlineContent: Map<String, InlineTextContent>,
    animateEmotes: Boolean,
    showTimestamps: Boolean
) {
    val timestamp = message.timestamp
        ?.formatTimestamp()
        ?.takeIf { showTimestamps }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (timestamp != null) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = timestamp,
                color = LocalContentColor.current.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }

        when (message) {
            is ChatEntry.Highlighted -> {
                HighlightedMessage(
                    message = message,
                    inlineContent = inlineContent,
                    animateEmotes = animateEmotes
                )
            }
            is ChatEntry.Simple -> {
                SimpleMessage(
                    message = message,
                    inlineContent = inlineContent,
                    animateEmotes = animateEmotes
                )
            }
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
                        modifier = Modifier.padding(4.dp),
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
                        modifier = modifier.padding(4.dp),
                        data = data,
                        inlineContent = inlineContent,
                        animateEmotes = animateEmotes
                    )
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
            modifier = modifier.padding(horizontal = 4.dp, vertical = 6.dp),
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
    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val fullInlineContent =
        inlineContent + data.emotes.orEmpty()
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

    val annotatedString = data.toAnnotatedString(fullInlineContent)

    Column {
        if (data.inReplyTo != null) {
            CompositionLocalProvider(
                LocalContentColor provides LocalContentColor.current.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(top = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp),
                        imageVector = Icons.Default.Reply,
                        contentDescription = "In reply to"
                    )

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("@${data.inReplyTo.userName}")
                            }

                            append(": ${data.inReplyTo.message}")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Text(
            modifier = modifier
                .pointerInput(annotatedString) {
                    forEachGesture {
                        coroutineScope {
                            awaitPointerEventScope {
                                // Wait for tap
                                awaitFirstDown().also { down ->
                                    // Check that text has been laid out (it should be)
                                    val layoutRes = layoutResult.value ?: return@also

                                    val position = layoutRes.getOffsetForPosition(down.position)
                                    val urlAnnotation =
                                        annotatedString.getStringAnnotations(position, position)
                                            .firstOrNull { it.tag == UrlAnnotationTag }

                                    if (urlAnnotation != null) {
                                        // Prevent parent components from getting the event,
                                        // we're dealing with it
                                        down.consume()

                                        // Wait for the user to stop clicking
                                        waitForUpOrCancellation()?.also { up ->
                                            // Tap on a link was successful, call onClick
                                            up.consume()
                                            uriHandler.openUri(urlAnnotation.item)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            onTextLayout = { layoutResult.value = it },
            text = annotatedString,
            inlineContent = fullInlineContent,
            lineHeight = 1.7.em,
            style = MaterialTheme.typography.bodyMedium
        )
    }
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
        badges?.forEach { badge ->
            appendInlineContent(
                id = badge.inlineContentId,
                alternateText = " "
            )

            append(' ')
        }

        withStyle(
            SpanStyle(
                color = Color(color),
                fontWeight = FontWeight.Bold
            )
        ) {
            withAnnotation(
                tag = UrlAnnotationTag,
                annotation = formatChannelUri(userName).toString()
            ) {
                append(userName)
            }

            if (!userName.matches(latinScriptUserName)) {
                append(" ($userLogin)")
            }

            append(if (isAction) " " else ": ")
        }

        message
            ?.let { message ->
                if (inReplyTo != null) message.removePrefix("@${inReplyTo.userName} ")
                else message
            }
            ?.split(' ')
            ?.forEach { word ->
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
                            withAnnotation(
                                tag = UrlAnnotationTag,
                                annotation = formatChannelUri(word.removePrefix("@")).toString()
                            ) {
                                append(word)
                            }
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

private val Badge.inlineContentId: String
    get() = "badge_${id}_$version"

private val TwitchBadge.inlineContentId: String
    get() = "badge_${id}_$version"
