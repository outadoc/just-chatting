package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.Badge
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.feature.chat.presentation.ChatPrefixConstants
import fr.outadoc.justchatting.utils.ui.ensureColorIsAccessible
import fr.outadoc.justchatting.utils.ui.parseHexColor
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentHashMap
import kotlin.random.Random

@Composable
fun ChatMessageBody(
    modifier: Modifier = Modifier,
    body: ChatEvent.Message.Body,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    knownChatters: PersistentSet<Chatter>,
    appUser: AppUser,
    backgroundHint: Color,
    richEmbed: ChatEvent.RichEmbed?,
) {
    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val fullInlineContent =
        inlineContent.toPersistentHashMap()
            .putAll(
                body.embeddedEmotes
                    .associate { emote ->
                        Pair(
                            emote.name,
                            emoteTextContent(
                                emote = emote,
                            ),
                        )
                    }
                    .toImmutableMap(),
            )

    val annotatedString = body.toAnnotatedString(
        appUser = appUser,
        inlineContent = fullInlineContent,
        knownChatters = knownChatters,
        backgroundHint = backgroundHint,
    )

    Column(modifier = modifier) {
        body.inReplyTo?.let { inReplyTo ->
            InReplyToMessage(
                modifier = Modifier.padding(bottom = 8.dp),
                appUserId = appUser.id,
                chatter = inReplyTo.chatter,
                message = inReplyTo.message,
            )
        }

        Text(
            modifier = Modifier
                .pointerInput(annotatedString) {
                    awaitEachGesture {
                        // Wait for tap
                        awaitFirstDown().also { down ->
                            // Check that text has been laid out (it should be)
                            val layoutRes = layoutResult.value ?: return@also

                            val position = layoutRes.getOffsetForPosition(down.position)
                            val urlAnnotation =
                                annotatedString
                                    .getStringAnnotations(position, position)
                                    .firstOrNull { it.tag == URL_ANNOTATION_TAG }

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
                },
            onTextLayout = { layoutResult.value = it },
            text = annotatedString,
            inlineContent = fullInlineContent,
            lineHeight = emoteSize,
            style = MaterialTheme.typography.bodyMedium.copy(
                hyphens = Hyphens.Auto,
            ),
        )

        AnimatedVisibility(visible = richEmbed != null) {
            if (richEmbed != null) {
                ChatRichEmbed(
                    modifier = Modifier.padding(top = 4.dp),
                    richEmbed = richEmbed,
                )
            }
        }
    }
}

@Stable
@Composable
@OptIn(ExperimentalTextApi::class)
fun ChatEvent.Message.Body.toAnnotatedString(
    appUser: AppUser,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    knownChatters: PersistentSet<Chatter>,
    urlColor: Color = MaterialTheme.colorScheme.primary,
    backgroundHint: Color = MaterialTheme.colorScheme.surface,
    mentionBackground: Color = MaterialTheme.colorScheme.onBackground,
    mentionColor: Color = MaterialTheme.colorScheme.background,
): AnnotatedString {
    val randomChatColors = integerArrayResource(R.array.randomChatColors).map { Color(it) }

    val color = remember(color) {
        color?.parseHexColor()?.let { color ->
            ensureColorIsAccessible(
                foreground = color,
                background = backgroundHint,
            )
        } ?: randomChatColors.random(
            Random(chatter.hashCode()),
        )
    }

    return buildAnnotatedString {
        badges.forEach { badge ->
            appendInlineContent(
                id = badge.inlineContentId,
                alternateText = " ",
            )

            append(' ')
        }

        withStyle(SpanStyle(color = color)) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                withAnnotation(
                    tag = URL_ANNOTATION_TAG,
                    annotation = chatter.login.createChannelDeeplink().toString(),
                ) {
                    append(chatter.displayName)
                }
            }

            if (chatter.hasLocalizedDisplayName) {
                append(" (${chatter.login})")
            }

            append(
                stringResource(
                    if (isAction) {
                        R.string.chat_message_actionSeparator
                    } else {
                        R.string.chat_message_standardSeparator
                    },
                ),
            )
        }

        message
            ?.stripReplyMention(inReplyTo)
            ?.split(' ')
            ?.forEach { word ->
                val mentionedChatter: Chatter? =
                    knownChatters.firstOrNull { chatter ->
                        chatter.matches(word.removePrefix(ChatPrefixConstants.ChatterPrefix.toString()))
                    }

                when {
                    word.matches(urlRegex) -> {
                        appendUrl(url = word, urlColor = urlColor)
                    }

                    mentionedChatter != null -> {
                        appendMention(
                            chatter = mentionedChatter,
                            appUser = appUser,
                            mentionBackground = mentionBackground,
                            mentionColor = mentionColor,
                        )
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

@OptIn(ExperimentalTextApi::class)
private fun AnnotatedString.Builder.appendUrl(url: String, urlColor: Color) {
    val validUrl: String = if (url.startsWith("http")) url else "https://$url"
    withStyle(SpanStyle(color = urlColor)) {
        withAnnotation(tag = URL_ANNOTATION_TAG, annotation = validUrl) {
            append(url)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun AnnotatedString.Builder.appendMention(
    chatter: Chatter,
    appUser: AppUser,
    mentionBackground: Color,
    mentionColor: Color,
) {
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
        withAnnotation(
            tag = URL_ANNOTATION_TAG,
            annotation = chatter.login.createChannelDeeplink().toString(),
        ) {
            withStyle(
                getMentionStyle(
                    mentioned = chatter.login == appUser.login,
                    mentionBackground = mentionBackground,
                    mentionColor = mentionColor,
                ),
            ) {
                append(ChatPrefixConstants.ChatterPrefix)
                append(chatter.displayName)
            }
        }
    }
}

private fun String.stripReplyMention(inReplyTo: ChatEvent.Message.Body.InReplyTo?): String {
    return inReplyTo?.let { replyTo -> removePrefix("@${replyTo.chatter.displayName} ") } ?: this
}

private val Badge.inlineContentId: String
    get() = "badge_${id}_$version"

private val urlRegex = Patterns.WEB_URL.toRegex()
private const val URL_ANNOTATION_TAG = "URL"
