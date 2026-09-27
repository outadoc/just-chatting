package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import com.materialkolor.ktx.harmonizeWithPrimary
import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.presentation.ChatterColors
import fr.outadoc.justchatting.feature.chat.presentation.MessageToken
import fr.outadoc.justchatting.feature.chat.presentation.displayedBadges
import fr.outadoc.justchatting.feature.chat.presentation.tokenize
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.chat_message_actionSeparator
import fr.outadoc.justchatting.shared.internal.chat_message_standardSeparator
import fr.outadoc.justchatting.utils.presentation.customColors
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentHashMap
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChatMessageBody(
    modifier: Modifier = Modifier,
    body: ChatListItem.Message.Body,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    emotes: ImmutableMap<String, Emote>,
    cheerEmotes: ImmutableMap<String, Emote>,
    pronouns: ImmutableMap<Chatter, Pronoun>,
    appUser: AppUser.LoggedIn,
    backgroundHint: Color,
    richEmbed: ChatListItem.RichEmbed?,
    maxLines: Int = Int.MAX_VALUE,
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val fullInlineContent =
        inlineContent
            .toPersistentHashMap()
            .putAll(
                body.embeddedEmotes
                    .associate { emote ->
                        Pair(
                            emote.name,
                            emoteTextContent(
                                emote = emote,
                                isGigantified = body.isGigantifiedEmote,
                            ),
                        )
                    }.toImmutableMap(),
            )

    val tokens: List<MessageToken> =
        remember(body, emotes, cheerEmotes, appUser) {
            body.tokenize(
                emotes = emotes,
                cheerEmotes = cheerEmotes,
                appUserLogin = appUser.userLogin,
            )
        }

    val annotatedMessage =
        body.toAnnotatedString(
            tokens = tokens,
            inlineContent = fullInlineContent,
            pronouns = pronouns,
            backgroundHint = backgroundHint,
        )

    val finalInlineContent = fullInlineContent.putAll(annotatedMessage.extraInlineContent)

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
            onTextLayout = { layoutResult.value = it },
            text = annotatedMessage.text,
            inlineContent = finalInlineContent,
            lineHeight = if (body.isGigantifiedEmote) gigantifiedEmoteSize else emoteSize,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    hyphens = Hyphens.Auto,
                ),
        )

        body.gifs.forEach { gif ->
            AsyncImage(
                modifier =
                    Modifier
                        .padding(top = 4.dp)
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                model = remoteImageModel(gif.url),
                contentDescription = gif.description,
                contentScale = ContentScale.Fit,
            )
        }

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

@Immutable
internal data class AnnotatedChatMessage(
    val text: AnnotatedString,
    val extraInlineContent: ImmutableMap<String, InlineTextContent>,
)

@Stable
@Composable
internal fun ChatListItem.Message.Body.toAnnotatedString(
    tokens: List<MessageToken>,
    inlineContent: ImmutableMap<String, InlineTextContent>,
    pronouns: ImmutableMap<Chatter, Pronoun>,
    urlColor: Color = MaterialTheme.colorScheme.primary,
    backgroundHint: Color = MaterialTheme.colorScheme.surface,
    mentionBackground: Color = MaterialTheme.colorScheme.onBackground,
    mentionColor: Color = MaterialTheme.colorScheme.background,
): AnnotatedChatMessage {
    val accessibleChatterColor: Color? =
        ChatterColors
            .accessibleColor(hexColor = color, background = backgroundHint.toArgb())
            ?.let { argb -> Color(argb) }

    val fallbackChatColors = MaterialTheme.customColors.fallbackChatColors
    val fallbackColor =
        fallbackChatColors.elementAt(
            ChatterColors.fallbackIndex(chatter = chatter, paletteSize = fallbackChatColors.size),
        )

    val pronoun: String? = pronouns[chatter]?.displayPronoun

    val extraInlineContent = mutableMapOf<String, InlineTextContent>()

    val text =
        buildAnnotatedString {
            sourceRoomId?.let { roomId ->
                val sourceChannelId = sourceChannelInlineContentId(roomId)
                if (sourceChannelId in inlineContent) {
                    appendInlineContent(
                        id = sourceChannelId,
                        alternateText = " ",
                    )

                    append(' ')
                }
            }

            if (pronoun != null) {
                withStyle(SpanStyle(fontSize = 0.8.em)) {
                    append("($pronoun) ")
                }
            }

            val (shownBadges, badgesRoomId) = displayedBadges

            shownBadges.forEach { badge ->
                val badgeId =
                    if (badgesRoomId != null) {
                        badge.sourceInlineContentId(badgesRoomId)
                    } else {
                        badge.inlineContentId
                    }

                appendInlineContent(
                    id = badgeId,
                    alternateText = " ",
                )

                append(' ')
            }

            if (isAction) {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
            }

            withStyle(
                SpanStyle(
                    color =
                        MaterialTheme.colorScheme.harmonizeWithPrimary(
                            accessibleChatterColor ?: fallbackColor,
                        ),
                ),
            ) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(chatter.displayName)
                }

                if (chatter.hasLocalizedDisplayName) {
                    append(" (${chatter.login})")
                }

                append(
                    stringResource(
                        if (isAction) {
                            Res.string.chat_message_actionSeparator
                        } else {
                            Res.string.chat_message_standardSeparator
                        },
                    ),
                )
            }

            tokens.forEach { token ->
                when (token) {
                    is MessageToken.Link -> {
                        appendUrl(text = token.text, url = token.url, urlColor = urlColor)
                    }

                    is MessageToken.Emote -> {
                        if (token.overlays.isEmpty()) {
                            appendInlineContent(
                                id = token.text,
                                alternateText = token.text,
                            )
                        } else {
                            // Zero-width emotes are drawn on top of the emote they follow
                            val groupId = zeroWidthGroupInlineContentId(token.text.split(' '))

                            extraInlineContent.getOrPut(groupId) {
                                zeroWidthEmoteTextContent(
                                    base = token.emote,
                                    overlays = token.overlays,
                                    isGigantified = isGigantifiedEmote,
                                )
                            }

                            appendInlineContent(
                                id = groupId,
                                alternateText = token.text,
                            )
                        }
                    }

                    is MessageToken.Mention -> {
                        withStyle(
                            getMentionStyle(
                                mentioned = token.isMentionOfAppUser,
                                mentionBackground = mentionBackground,
                                mentionColor = mentionColor,
                            ),
                        ) {
                            append(token.text)
                        }
                    }

                    is MessageToken.Word -> {
                        append(token.text)
                    }
                }

                append(' ')
            }
        }

    return AnnotatedChatMessage(
        text = text,
        extraInlineContent = extraInlineContent.toImmutableMap(),
    )
}

private fun AnnotatedString.Builder.appendUrl(
    text: String,
    url: String,
    urlColor: Color,
) {
    withLink(
        LinkAnnotation.Url(
            url,
            TextLinkStyles(
                style =
                    SpanStyle(
                        color = urlColor,
                        textDecoration = TextDecoration.Underline,
                    ),
            ),
        ),
    ) {
        append(text)
    }
}

private val Badge.inlineContentId: String
    get() = "badge_${id}_$version"

private fun Badge.sourceInlineContentId(roomId: String): String = "source_badge:$roomId:${id}_$version"
