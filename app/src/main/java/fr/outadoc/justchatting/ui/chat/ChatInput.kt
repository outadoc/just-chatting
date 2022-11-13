package fr.outadoc.justchatting.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.composepreview.ThemePreviews
import fr.outadoc.justchatting.model.chat.Chatter
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.ui.HapticIconButton
import fr.outadoc.justchatting.ui.theme.AppTheme
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.ui.view.emotes.EmoteItem
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    inputState: ChatViewModel.InputState,
    animateEmotes: Boolean,
    onEmoteClick: (Emote) -> Unit,
    onChatterClick: (Chatter) -> Unit,
    onMessageChange: (TextFieldValue) -> Unit,
    onToggleEmotePicker: () -> Unit,
    onClearReplyingTo: () -> Unit,
    onSubmit: () -> Unit
) {
    when (state) {
        ChatViewModel.State.Initial -> {}
        is ChatViewModel.State.Chatting -> {
            Surface(
                shadowElevation = 2.dp,
                tonalElevation = 1.dp
            ) {
                ChatInput(
                    modifier = modifier,
                    message = inputState.inputMessage,
                    previousWord = inputState.previousWord,
                    emotes = state.allEmotes,
                    chatters = state.chatters,
                    replyingTo = inputState.replyingTo,
                    animateEmotes = animateEmotes,
                    onEmoteClick = onEmoteClick,
                    onChatterClick = onChatterClick,
                    onMessageChange = onMessageChange,
                    onToggleEmotePicker = onToggleEmotePicker,
                    onClearReplyingTo = onClearReplyingTo,
                    onSubmit = onSubmit
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun ChatInputPreviewBasic() {
    AppTheme {
        ChatInput(
            message = TextFieldValue("Lorem ipsum KEKW")
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
            )
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
            replyingTo = ChatEntry.Simple(
                data = ChatEntry.Data(
                    message = "Lorem ipsum dolor sit amet?",
                    messageId = "",
                    userId = "",
                    userName = "AntoineDaniel",
                    userLogin = "",
                    isAction = false,
                    color = null,
                    emotes = null,
                    badges = null,
                    inReplyTo = null
                ),
                timestamp = Instant.parse("2022-01-01T00:00:00.00Z")
            )
        )
    }
}

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    message: TextFieldValue = TextFieldValue(),
    previousWord: CharSequence = "",
    emotes: ImmutableSet<Emote> = persistentSetOf(),
    chatters: ImmutableSet<Chatter> = persistentSetOf(),
    animateEmotes: Boolean = true,
    replyingTo: ChatEntry? = null,
    onEmoteClick: (Emote) -> Unit = {},
    onChatterClick: (Chatter) -> Unit = {},
    onMessageChange: (TextFieldValue) -> Unit = {},
    onToggleEmotePicker: () -> Unit = {},
    onClearReplyingTo: () -> Unit = {},
    onSubmit: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    Column {
        AnimatedVisibility(visible = replyingTo?.data != null) {
            Row(
                modifier = Modifier.padding(
                    top = 2.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InReplyToMessage(
                    modifier = Modifier.weight(1f),
                    userName = replyingTo?.data?.userName.orEmpty(),
                    message = replyingTo?.data?.message.orEmpty()
                )

                HapticIconButton(onClick = onClearReplyingTo) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(R.string.chat_input_replyClear)
                    )
                }
            }
        }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChatInputAutoComplete(
                modifier = Modifier.weight(1f, fill = true),
                previousWord = previousWord,
                emotes = emotes,
                chatters = chatters,
                animateEmotes = animateEmotes,
                onEmoteClick = onEmoteClick,
                onChatterClick = onChatterClick
            ) {
                ChatTextField(
                    modifier = Modifier.fillMaxWidth(),
                    message = message,
                    onMessageChange = onMessageChange,
                    onToggleEmotePicker = onToggleEmotePicker,
                    onSubmit = onSubmit
                )
            }

            AnimatedVisibility(visible = message.text.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSubmit()
                    }
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = stringResource(R.string.chat_input_send_cd)
                    )
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
    onSubmit: () -> Unit
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
            capitalization = KeyboardCapitalization.Sentences
        ),
        keyboardActions = KeyboardActions(
            onSend = { onSubmit() }
        ),
        placeholder = {
            Text(text = stringResource(R.string.chat_input_hint))
        },
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        leadingIcon = {
            HapticIconButton(onClick = onToggleEmotePicker) {
                Icon(
                    Icons.Default.Mood,
                    contentDescription = stringResource(R.string.chat_input_emote_cd)
                )
            }
        },
        trailingIcon = {
            if (message.text.isNotEmpty()) {
                HapticIconButton(
                    onClick = { onMessageChange(TextFieldValue("")) }
                ) {
                    Icon(
                        Icons.Filled.Cancel,
                        contentDescription = stringResource(R.string.chat_input_clear_cd)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputAutoComplete(
    modifier: Modifier = Modifier,
    previousWord: CharSequence,
    emotes: ImmutableSet<Emote>,
    chatters: ImmutableSet<Chatter>,
    animateEmotes: Boolean,
    onEmoteClick: (Emote) -> Unit,
    onChatterClick: (Chatter) -> Unit,
    block: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var hiddenByUser by remember(previousWord) { mutableStateOf(false) }
    var matchingEmotes by remember { mutableStateOf(emptyList<Emote>()) }
    var matchingChatters by remember { mutableStateOf(emptyList<Chatter>()) }

    LaunchedEffect(previousWord) {
        withContext(Dispatchers.Default) {
            val emoteFilter = previousWord
                .takeIf { it.startsWith(':') }
                ?.trimStart(':')
                ?.takeIf { it.isNotEmpty() }

            matchingEmotes =
                if (emoteFilter == null) emptyList()
                else emotes.filter { emote ->
                    emote.name.contains(emoteFilter, ignoreCase = true)
                }
        }
    }

    LaunchedEffect(previousWord) {
        withContext(Dispatchers.Default) {
            val chatterFilter = previousWord
                .takeIf { it.startsWith('@') }
                ?.trimStart('@')
                ?.takeIf { it.isNotEmpty() }

            matchingChatters =
                if (chatterFilter == null) emptyList()
                else chatters.filter { chatter ->
                    chatter.name.startsWith(chatterFilter, ignoreCase = true)
                }
        }
    }

    val isVisible = !hiddenByUser && (matchingChatters.isNotEmpty() || matchingEmotes.isNotEmpty())

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isVisible,
        onExpandedChange = { hiddenByUser = true }
    ) {
        block()

        ExposedDropdownMenu(
            expanded = isVisible,
            onDismissRequest = { hiddenByUser = true }
        ) {
            matchingEmotes.forEach { emote ->
                DropdownMenuItem(
                    leadingIcon = {
                        EmoteItem(
                            modifier = Modifier.size(32.dp),
                            emote = emote,
                            animateEmotes = animateEmotes
                        )
                    },
                    text = { Text(text = emote.name) },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEmoteClick(emote)
                    }
                )
            }

            matchingChatters.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(text = selectionOption.name) },
                    onClick = { onChatterClick(selectionOption) }
                )
            }
        }
    }
}
