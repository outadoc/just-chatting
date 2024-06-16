package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.feature.chat.presentation.AutoCompleteItem
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.chat_input_clear_cd
import fr.outadoc.justchatting.shared.chat_input_emote_cd
import fr.outadoc.justchatting.shared.chat_input_hint
import fr.outadoc.justchatting.shared.chat_input_replyClear
import fr.outadoc.justchatting.shared.chat_input_send_cd
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.HapticIconButton
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import kotlinx.datetime.Instant

@ThemePreviews
@Composable
fun ChatInputPreviewBasic() {
    AppTheme {
        ChatInput(
            message = TextFieldValue("Lorem ipsum KEKW"),
        )
    }
}

@ThemePreviews
@Composable
fun ChatInputPreviewLongMessage() {
    AppTheme {
        ChatInput(
            message = TextFieldValue(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque at arcu at neque tempus sollicitudin.",
            ),
        )
    }
}

@ThemePreviews
@Composable
fun ChatInputPreviewEmpty() {
    AppTheme {
        ChatInput()
    }
}

@ThemePreviews
@Composable
fun ChatInputPreviewReplying() {
    AppTheme {
        ChatInput(
            replyingTo = ChatEvent.Message.Simple(
                body = ChatEvent.Message.Body(
                    message = "Lorem ipsum dolor sit amet?",
                    messageId = "",
                    chatter = Chatter(
                        id = "",
                        displayName = "AntoineDaniel",
                        login = "",
                    ),
                ),
                timestamp = Instant.parse("2022-01-01T00:00:00.00Z"),
            ),
        )
    }
}

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    appUser: AppUser.LoggedIn? = null,
    message: TextFieldValue = TextFieldValue(),
    autoCompleteItems: List<AutoCompleteItem> = emptyList(),
    replyingTo: ChatEvent.Message? = null,
    onEmoteClick: (Emote) -> Unit = {},
    onChatterClick: (Chatter) -> Unit = {},
    onMessageChange: (TextFieldValue) -> Unit = {},
    onToggleEmotePicker: () -> Unit = {},
    onClearReplyingTo: () -> Unit = {},
    onTriggerAutoComplete: () -> Unit = {},
    onSubmit: () -> Unit = {},
    isSubmitVisible: Boolean = true,
    isSubmitEnabled: Boolean = true,
    contentPadding: Dp = 0.dp,
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier.padding(
            bottom = contentPadding,
        ),
    ) {
        val replyingToMessage = replyingTo?.body
        AnimatedVisibility(visible = replyingToMessage != null) {
            if (replyingToMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(
                                start = contentPadding,
                                end = contentPadding,
                            ),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        InReplyToMessage(
                            modifier = Modifier.weight(1f),
                            appUser = appUser,
                            mentions = listOf(replyingToMessage.chatter.displayName),
                            message = replyingToMessage.message.orEmpty(),
                        )

                        HapticIconButton(onClick = onClearReplyingTo) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(Res.string.chat_input_replyClear),
                            )
                        }
                    }
                }
            }
        }

        Column {
            AnimatedVisibility(visible = autoCompleteItems.isNotEmpty()) {
                ChatAutoCompleteRow(
                    modifier = Modifier.padding(
                        top = 8.dp,
                    ),
                    onChatterClick = onChatterClick,
                    onEmoteClick = onEmoteClick,
                    items = autoCompleteItems,
                    contentPadding = PaddingValues(
                        horizontal = contentPadding,
                    ),
                )
            }

            Row(
                modifier = Modifier.padding(
                    top = 8.dp,
                    start = contentPadding,
                    end = contentPadding,
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChatTextField(
                    modifier = Modifier.weight(1f, fill = true),
                    message = message,
                    onMessageChange = onMessageChange,
                    onToggleEmotePicker = onToggleEmotePicker,
                    onTriggerAutoComplete = onTriggerAutoComplete,
                    onSubmit = onSubmit,
                )

                AnimatedVisibility(visible = isSubmitVisible && message.text.isNotEmpty()) {
                    Button(
                        modifier = Modifier.size(56.0.dp),
                        contentPadding = PaddingValues(4.dp),
                        enabled = isSubmitEnabled,
                        shape = MaterialTheme.shapes.large,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSubmit()
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(Res.string.chat_input_send_cd),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTextField(
    modifier: Modifier = Modifier,
    message: TextFieldValue,
    onMessageChange: (TextFieldValue) -> Unit,
    onToggleEmotePicker: () -> Unit,
    onTriggerAutoComplete: () -> Unit,
    onSubmit: () -> Unit,
) {
    TextField(
        modifier = modifier
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.Tab -> {
                            onTriggerAutoComplete()
                            true
                        }

                        Key.Enter -> {
                            onSubmit()
                            true
                        }

                        else -> false
                    }
                } else {
                    false
                }
            },
        value = message,
        singleLine = false,
        onValueChange = onMessageChange,
        shape = FloatingActionButtonDefaults.shape,
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Send,
            capitalization = KeyboardCapitalization.Sentences,
        ),
        keyboardActions = KeyboardActions(
            onSend = { onSubmit() },
        ),
        placeholder = {
            Text(text = stringResource(Res.string.chat_input_hint))
        },
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        leadingIcon = {
            HapticIconButton(onClick = onToggleEmotePicker) {
                Icon(
                    Icons.Default.Mood,
                    contentDescription = stringResource(Res.string.chat_input_emote_cd),
                )
            }
        },
        trailingIcon = {
            if (message.text.isNotEmpty()) {
                HapticIconButton(
                    onClick = { onMessageChange(TextFieldValue("")) },
                ) {
                    Icon(
                        Icons.Filled.Cancel,
                        contentDescription = stringResource(Res.string.chat_input_clear_cd),
                    )
                }
            }
        },
    )
}
