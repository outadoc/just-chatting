package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.presentation.AutoCompleteItem
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
                data = ChatEvent.Data(
                    message = "Lorem ipsum dolor sit amet?",
                    messageId = "",
                    userId = "",
                    userName = "AntoineDaniel",
                    userLogin = "",
                    isAction = false,
                    color = null,
                    embeddedEmotes = null,
                    badges = null,
                    inReplyTo = null,
                ),
                timestamp = Instant.parse("2022-01-01T00:00:00.00Z"),
            ),
        )
    }
}

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    message: TextFieldValue = TextFieldValue(),
    autoCompleteItems: List<AutoCompleteItem> = emptyList(),
    animateEmotes: Boolean = true,
    replyingTo: ChatEvent.Message? = null,
    onEmoteClick: (Emote) -> Unit = {},
    onChatterClick: (Chatter) -> Unit = {},
    onMessageChange: (TextFieldValue) -> Unit = {},
    onToggleEmotePicker: () -> Unit = {},
    onClearReplyingTo: () -> Unit = {},
    onSubmit: () -> Unit = {},
    canSubmit: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current

    Column {
        AnimatedVisibility(visible = replyingTo?.data != null) {
            Row(
                modifier = Modifier.padding(
                    top = 2.dp,
                    start = 8.dp,
                    end = 8.dp,
                ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InReplyToMessage(
                    modifier = Modifier.weight(1f),
                    userName = replyingTo?.data?.userName.orEmpty(),
                    message = replyingTo?.data?.message.orEmpty(),
                )

                HapticIconButton(onClick = onClearReplyingTo) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(R.string.chat_input_replyClear),
                    )
                }
            }
        }

        Column(modifier = modifier) {
            AnimatedVisibility(visible = autoCompleteItems.isNotEmpty()) {
                ChatAutoCompleteRow(
                    onChatterClick = onChatterClick,
                    onEmoteClick = onEmoteClick,
                    items = autoCompleteItems,
                    animateEmotes = animateEmotes,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChatTextField(
                    modifier = Modifier.weight(1f, fill = true),
                    message = message,
                    onMessageChange = onMessageChange,
                    onToggleEmotePicker = onToggleEmotePicker,
                    onSubmit = onSubmit,
                )

                AnimatedVisibility(visible = canSubmit && message.text.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSubmit()
                        },
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = stringResource(R.string.chat_input_send_cd),
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
    onSubmit: () -> Unit,
) {
    TextField(
        modifier = modifier,
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
            Text(text = stringResource(R.string.chat_input_hint))
        },
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        leadingIcon = {
            HapticIconButton(onClick = onToggleEmotePicker) {
                Icon(
                    Icons.Default.Mood,
                    contentDescription = stringResource(R.string.chat_input_emote_cd),
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
                        contentDescription = stringResource(R.string.chat_input_clear_cd),
                    )
                }
            }
        },
    )
}
