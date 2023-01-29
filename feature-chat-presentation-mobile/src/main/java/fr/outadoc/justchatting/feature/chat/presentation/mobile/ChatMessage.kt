package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.domain.model.CheerEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.Emote
import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchBadge
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.feature.chat.data.model.Badge
import fr.outadoc.justchatting.feature.chat.presentation.ChatEntry
import fr.outadoc.justchatting.feature.chat.presentation.ChatViewModel
import fr.outadoc.justchatting.feature.chat.presentation.RoomState
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.ChatEntryPreviewProvider
import fr.outadoc.justchatting.feature.chat.presentation.mobile.preview.previewBadges
import fr.outadoc.justchatting.utils.core.isOdd
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import fr.outadoc.justchatting.utils.ui.ensureColorIsAccessible
import fr.outadoc.justchatting.utils.ui.formatTimestamp
import fr.outadoc.justchatting.utils.ui.parseHexColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentHashMap
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration

private val urlRegex = Patterns.WEB_URL.toRegex()
private const val UrlAnnotationTag = "URL"

private val asciiEncoder = Charsets.US_ASCII.newEncoder()

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    animateEmotes: Boolean,
    showTimestamps: Boolean,
    onMessageLongClick: (ChatEntry) -> Unit,
    onReplyToMessage: (ChatEntry) -> Unit,
    insets: PaddingValues,
) {
    when (state) {
        ChatViewModel.State.Initial -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        }

        is ChatViewModel.State.Chatting -> {
            if (state.chatMessages.isEmpty()) {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                ChatList(
                    modifier = modifier,
                    state = state,
                    animateEmotes = animateEmotes,
                    showTimestamps = showTimestamps,
                    onMessageLongClick = onMessageLongClick,
                    onReplyToMessage = onReplyToMessage,
                    insets = insets,
                )
            }
        }
    }
}

@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State.Chatting,
    animateEmotes: Boolean,
    showTimestamps: Boolean,
    onMessageLongClick: (ChatEntry) -> Unit,
    onReplyToMessage: (ChatEntry) -> Unit,
    insets: PaddingValues,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val haptic = LocalHapticFeedback.current

    var wasListScrolledByUser by remember { mutableStateOf(false) }

    if (listState.isScrollInProgress) {
        wasListScrolledByUser = true
    }

    LaunchedEffect(state.chatMessages, wasListScrolledByUser) {
        if (!wasListScrolledByUser) {
            listState.scrollToItem(
                index = (state.chatMessages.size - 1).coerceAtLeast(0),
            )
        }
    }

    Box(modifier = modifier) {
        ChatList(
            entries = state.chatMessages,
            emotes = state.allEmotesMap,
            cheerEmotes = state.cheerEmotes,
            badges = state.globalBadges.addAll(state.channelBadges),
            roomState = state.roomState,
            animateEmotes = animateEmotes,
            showTimestamps = showTimestamps,
            isDisconnected = !state.connectionStatus.isAlive,
            listState = listState,
            onMessageLongClick = onMessageLongClick,
            onReplyToMessage = onReplyToMessage,
            appUser = state.appUser,
            insets = insets,
        )

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = wasListScrolledByUser,
        ) {
            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(insets),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        wasListScrolledByUser = false
                        listState.scrollToItem(
                            index = (state.chatMessages.size - 1).coerceAtLeast(0),
                        )
                    }
                },
            ) {
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = stringResource(R.string.scroll_down),
                )
            }
        }
    }
}

@Composable
fun RoomStateBanner(
    modifier: Modifier = Modifier,
    roomState: RoomState,
) {
    SlimSnackbar(modifier = modifier) {
        with(roomState) {
            if (isEmoteOnly) {
                Text(text = stringResource(R.string.room_emote))
            }

            if (minFollowDuration != null) {
                Text(
                    text = when (minFollowDuration) {
                        Duration.ZERO -> stringResource(R.string.room_followers)
                        else -> stringResource(
                            R.string.room_followers_min,
                            minFollowDuration.toString(),
                        )
                    },
                )
            }

            if (uniqueMessagesOnly) {
                Text(text = stringResource(R.string.room_unique))
            }

            if (slowModeDuration != null) {
                Text(
                    text = stringResource(
                        R.string.room_slow,
                        slowModeDuration.toString(),
                    ),
                )
            }

            if (isSubOnly) {
                Text(text = stringResource(R.string.room_subs))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    entries: ImmutableList<ChatEntry>,
    emotes: ImmutableMap<String, Emote>,
    cheerEmotes: ImmutableMap<String, CheerEmote>,
    badges: ImmutableList<TwitchBadge>,
    animateEmotes: Boolean,
    showTimestamps: Boolean,
    isDisconnected: Boolean,
    listState: LazyListState,
    onMessageLongClick: (ChatEntry) -> Unit,
    onReplyToMessage: (ChatEntry) -> Unit,
    roomState: RoomState,
    appUser: AppUser,
    insets: PaddingValues,
) {
    val inlinesEmotes: PersistentMap<String, InlineTextContent> =
        remember(emotes) {
            emotes.mapValues { (_, emote) ->
                emoteTextContent(
                    emote = emote,
                    animateEmotes = animateEmotes,
                )
            }.toPersistentHashMap()
        }

    val inlineBadges: PersistentMap<String, InlineTextContent> =
        remember(badges) {
            badges.associate { badge ->
                Pair(
                    badge.inlineContentId,
                    badgeTextContent(
                        badge = badge,
                    ),
                )
            }.toPersistentHashMap()
        }

    val inlineCheerEmotes: PersistentMap<String, InlineTextContent> =
        remember(cheerEmotes) {
            cheerEmotes.mapValues { (_, cheer) ->
                cheerEmoteTextContent(
                    cheer = cheer,
                    animateEmotes = animateEmotes,
                )
            }.toPersistentHashMap()
        }

    val inlineContent: PersistentMap<String, InlineTextContent> =
        remember(inlinesEmotes, inlineBadges, inlineCheerEmotes) {
            inlinesEmotes
                .putAll(inlineBadges)
                .putAll(inlineCheerEmotes)
        }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(
            bottom = insets.calculateBottomPadding(),
        ),
    ) {
        stickyHeader {
            Column(
                modifier = Modifier.padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Spacer(
                    modifier = Modifier.padding(
                        top = insets.calculateTopPadding(),
                    ),
                )
                AnimatedVisibility(
                    visible = !roomState.isDefault,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) {
                    RoomStateBanner(
                        modifier = Modifier.fillMaxWidth(),
                        roomState = roomState,
                    )
                }

                AnimatedVisibility(
                    visible = isDisconnected,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) {
                    SlimSnackbar(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(text = stringResource(R.string.connectionLost_error))
                    }
                }
            }
        }

        itemsIndexed(
            items = entries,
            key = { _, item -> item.hashCode() },
            contentType = { _, item ->
                when (item) {
                    is ChatEntry.Highlighted -> 1
                    is ChatEntry.Simple -> 2
                }
            },
        ) { index, item ->
            val background =
                if (index.isOdd) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.surface
                }

            val canBeRepliedTo = item.data?.messageId != null
            val replyToActionCd = stringResource(R.string.chat_replyTo)

            SwipeToReply(
                onDismiss = { onReplyToMessage(item) },
                enabled = canBeRepliedTo,
            ) {
                ChatMessage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { onMessageLongClick(item) },
                            onLongClickLabel = stringResource(R.string.chat_copyToClipboard),
                        )
                        .semantics {
                            if (canBeRepliedTo) {
                                customActions = listOf(
                                    CustomAccessibilityAction(replyToActionCd) {
                                        onReplyToMessage(item)
                                        true
                                    },
                                )
                            }
                        },
                    message = item,
                    inlineContent = inlineContent,
                    animateEmotes = animateEmotes,
                    showTimestamps = showTimestamps,
                    background = background,
                    appUser = appUser,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToReply(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToEnd) onDismiss()
            it != DismissValue.DismissedToEnd
        },
    )

    SwipeToDismiss(
        modifier = modifier,
        state = dismissState,
        directions = if (enabled) setOf(DismissDirection.StartToEnd) else emptySet(),
        dismissThresholds = { FractionalThreshold(0.15f) },
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            if (direction != DismissDirection.StartToEnd) return@SwipeToDismiss

            val scale by animateFloatAsState(
                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f,
            )

            val haptic = LocalHapticFeedback.current
            LaunchedEffect(dismissState.targetValue) {
                if (dismissState.targetValue == DismissValue.DismissedToEnd) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    Icons.Default.Reply,
                    contentDescription = "Reply",
                    modifier = Modifier.scale(scale),
                )
            }
        },
    ) {
        val elevation = animateDpAsState(if (dismissState.dismissDirection != null) 4.dp else 0.dp)
        Surface(shadowElevation = elevation.value) {
            content()
        }
    }
}

@ThemePreviews
@Composable
fun ChatMessagePreview(
    @PreviewParameter(ChatEntryPreviewProvider::class) chatEntry: ChatEntry,
) {
    val inlineBadges = previewBadges
        .associateWith { previewTextContent() }
        .toPersistentHashMap()

    AppTheme {
        ChatMessage(
            message = chatEntry,
            inlineContent = inlineBadges,
            animateEmotes = true,
            showTimestamps = true,
            appUser = AppUser.LoggedIn(
                id = "123",
                login = "outadoc",
                helixToken = "",
            ),
        )
    }
}

@Composable
fun ChatMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    animateEmotes: Boolean,
    showTimestamps: Boolean,
    background: Color = Color.Transparent,
    appUser: AppUser,
) {
    val timestamp = message.timestamp
        .formatTimestamp()
        ?.takeIf { showTimestamps }

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .background(background)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (timestamp != null) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = timestamp,
                color = LocalContentColor.current.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        when (message) {
            is ChatEntry.Highlighted -> {
                HighlightedMessage(
                    message = message,
                    inlineContent = inlineContent,
                    animateEmotes = animateEmotes,
                    appUser = appUser,
                )
            }

            is ChatEntry.Simple -> {
                SimpleMessage(
                    message = message,
                    inlineContent = inlineContent,
                    animateEmotes = animateEmotes,
                    appUser = appUser,
                )
            }
        }
    }
}

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

@Composable
fun SimpleMessage(
    modifier: Modifier = Modifier,
    message: ChatEntry.Simple,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    animateEmotes: Boolean,
    appUser: AppUser,
    backgroundHint: Color = MaterialTheme.colorScheme.surface,
) {
    Row {
        Spacer(modifier = Modifier.width(4.dp))

        ChatMessageData(
            modifier = modifier.padding(
                horizontal = 4.dp,
                vertical = 6.dp,
            ),
            data = message.data,
            inlineContent = inlineContent,
            animateEmotes = animateEmotes,
            appUser = appUser,
            backgroundHint = backgroundHint,
        )
    }
}

@Composable
fun ChatMessageData(
    modifier: Modifier = Modifier,
    data: ChatEntry.Data,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    animateEmotes: Boolean,
    appUser: AppUser,
    backgroundHint: Color,
) {
    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val fullInlineContent =
        inlineContent.toPersistentHashMap()
            .putAll(
                data.emotes.orEmpty()
                    .associate { emote ->
                        Pair(
                            emote.name,
                            emoteTextContent(
                                emote = emote,
                                animateEmotes = animateEmotes,
                            ),
                        )
                    }
                    .toImmutableMap(),
            )

    val annotatedString = data.toAnnotatedString(
        appUser = appUser,
        inlineContent = fullInlineContent,
        backgroundHint = backgroundHint,
    )

    Column(modifier = modifier) {
        data.inReplyTo?.let { inReplyTo ->
            InReplyToMessage(
                modifier = Modifier.padding(bottom = 8.dp),
                userName = inReplyTo.userName,
                message = inReplyTo.message,
            )
        }

        Text(
            modifier = Modifier
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
                                        annotatedString
                                            .getStringAnnotations(position, position)
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
            lineHeight = emoteSize,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

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

@Stable
@Composable
@OptIn(ExperimentalTextApi::class)
fun ChatEntry.Data.toAnnotatedString(
    appUser: AppUser,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    urlColor: Color = MaterialTheme.colorScheme.primary,
    backgroundHint: Color = MaterialTheme.colorScheme.surface,
    mentionBackground: Color = MaterialTheme.colorScheme.onBackground,
    mentionColor: Color = MaterialTheme.colorScheme.background,
): AnnotatedString {
    val randomChatColors = integerArrayResource(R.array.randomChatColors).map { Color(it) }

    val color = remember(color) {
        color?.let { color ->
            ensureColorIsAccessible(
                foreground = color.parseHexColor(),
                background = backgroundHint,
            )
        } ?: randomChatColors.random(
            Random(userName.hashCode()),
        )
    }

    return buildAnnotatedString {
        badges?.forEach { badge ->
            appendInlineContent(
                id = badge.inlineContentId,
                alternateText = " ",
            )

            append(' ')
        }

        withStyle(SpanStyle(color = color)) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                withAnnotation(
                    tag = UrlAnnotationTag,
                    annotation = userName.createChannelDeeplink().toString(),
                ) {
                    append(userName)
                }
            }

            if (!asciiEncoder.canEncode(userName)) {
                append(" ($userLogin)")
            }

            append(if (isAction) " " else ": ")
        }

        message
            ?.let { message ->
                inReplyTo?.let { inReplyTo ->
                    message.removePrefix("@${inReplyTo.userName} ")
                } ?: message
            }
            ?.split(' ')
            ?.forEach { word ->
                when {
                    word.matches(urlRegex) -> {
                        val url = if (word.startsWith("http")) word else "https://$word"
                        withStyle(SpanStyle(color = urlColor)) {
                            withAnnotation(tag = UrlAnnotationTag, annotation = url) {
                                append(word)
                            }
                        }
                    }

                    word.startsWith('@') -> {
                        val username = word.removePrefix("@")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            withAnnotation(
                                tag = UrlAnnotationTag,
                                annotation = username.createChannelDeeplink().toString(),
                            ) {
                                if (username == appUser.login) {
                                    withStyle(
                                        SpanStyle(
                                            background = mentionBackground,
                                            color = mentionColor,
                                        ),
                                    ) {
                                        append(word)
                                    }
                                } else {
                                    append(word)
                                }
                            }
                        }
                    }

                    word in inlineContent -> {
                        appendInlineContent(
                            id = word,
                            alternateText = word,
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

private val Badge.inlineContentId: String
    get() = "badge_${id}_$version"

private val TwitchBadge.inlineContentId: String
    get() = "badge_${id}_$version"
