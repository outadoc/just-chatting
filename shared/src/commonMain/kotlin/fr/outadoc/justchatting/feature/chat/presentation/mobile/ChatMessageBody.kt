package fr.outadoc.justchatting.feature.chat.presentation.mobile

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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.materialkolor.ktx.harmonizeWithPrimary
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.presentation.ChatPrefixConstants
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.customColors
import fr.outadoc.justchatting.utils.presentation.ensureColorIsAccessible
import fr.outadoc.justchatting.utils.presentation.isValidWebUrl
import fr.outadoc.justchatting.utils.presentation.parseHexColor
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentHashMap
import kotlin.random.Random

@Composable
internal fun ChatMessageBody(
    modifier: Modifier = Modifier,
    body: ChatListItem.Message.Body,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    pronouns: ImmutableMap<Chatter, Pronoun>,
    appUser: AppUser.LoggedIn,
    backgroundHint: Color,
    richEmbed: ChatListItem.RichEmbed?,
    maxLines: Int = Int.MAX_VALUE,
    onShowInfoForUserId: (String) -> Unit = {},
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
        pronouns = pronouns,
        backgroundHint = backgroundHint,
    )

    Column(modifier = modifier) {
        body.inReplyTo?.let { inReplyTo ->
            InReplyToMessage(
                modifier = Modifier.padding(bottom = 8.dp),
                appUser = appUser,
                mentions = inReplyTo.mentions,
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

                            annotatedString
                                .getStringAnnotations(position, position)
                                .forEach { annotation ->
                                    when (annotation.tag) {
                                        URL_ANNOTATION_TAG -> {
                                            down.consume()
                                            waitForUpOrCancellation()?.also { up ->
                                                up.consume()
                                                uriHandler.openUri(annotation.item)
                                            }
                                        }

                                        CHATTER_ID_ANNOTATION_TAG -> {
                                            down.consume()
                                            waitForUpOrCancellation()?.also { up ->
                                                up.consume()
                                                onShowInfoForUserId(annotation.item)
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
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
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
internal fun ChatListItem.Message.Body.toAnnotatedString(
    appUser: AppUser.LoggedIn,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    pronouns: ImmutableMap<Chatter, Pronoun>,
    urlColor: Color = MaterialTheme.colorScheme.primary,
    backgroundHint: Color = MaterialTheme.colorScheme.surface,
    mentionBackground: Color = MaterialTheme.colorScheme.onBackground,
    mentionColor: Color = MaterialTheme.colorScheme.background,
): AnnotatedString {
    val accessibleChatterColor: Color? =
        color?.parseHexColor()?.let { rawColor ->
            ensureColorIsAccessible(rawColor, backgroundHint)
        }

    val randomChatColors = MaterialTheme.customColors.fallbackChatColors
    val fallbackColor = remember(chatter) {
        randomChatColors.random(Random(chatter.hashCode()))
    }

    val pronoun: String? = pronouns[chatter]?.displayPronoun

    return buildAnnotatedString {
        if (pronoun != null) {
            withStyle(SpanStyle(fontSize = 0.8.em)) {
                append("($pronoun) ")
            }
        }

        badges.forEach { badge ->
            appendInlineContent(
                id = badge.inlineContentId,
                alternateText = " ",
            )

            append(' ')
        }

        withStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.harmonizeWithPrimary(
                    accessibleChatterColor ?: fallbackColor,
                ),
            ),
        ) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                withAnnotation(
                    tag = CHATTER_ID_ANNOTATION_TAG,
                    annotation = chatter.id,
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
                        MR.strings.chat_message_actionSeparator
                    } else {
                        MR.strings.chat_message_standardSeparator
                    },
                ),
            )
        }

        message
            ?.split(' ')
            ?.forEach { word ->
                when {
                    word.isValidWebUrl() -> {
                        // This is a URL
                        appendUrl(url = word, urlColor = urlColor)
                    }

                    word in inlineContent -> {
                        // This is an emote
                        appendInlineContent(
                            id = word,
                            alternateText = word,
                        )
                    }

                    word.startsWith(ChatPrefixConstants.ChatterPrefix) -> {
                        // This is a user mention
                        appendMention(
                            mention = word,
                            appUser = appUser,
                            mentionBackground = mentionBackground,
                            mentionColor = mentionColor,
                        )
                    }

                    else -> {
                        // Just a normal word living in a normal world
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

private fun AnnotatedString.Builder.appendMention(
    mention: String,
    appUser: AppUser.LoggedIn,
    mentionBackground: Color,
    mentionColor: Color,
) {
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
        withStyle(
            getMentionStyle(
                // TODO also check for userDisplayName
                mentioned = mention.contentEquals(appUser.userLogin, ignoreCase = true),
                mentionBackground = mentionBackground,
                mentionColor = mentionColor,
            ),
        ) {
            append(mention)
        }
    }
}

private val Badge.inlineContentId: String
    get() = "badge_${id}_$version"

private const val URL_ANNOTATION_TAG = "URL"
private const val CHATTER_ID_ANNOTATION_TAG = "USER_ID"
