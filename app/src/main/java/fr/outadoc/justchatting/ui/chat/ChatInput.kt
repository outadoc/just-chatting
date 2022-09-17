package fr.outadoc.justchatting.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.chat.Chatter
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import fr.outadoc.justchatting.ui.view.emotes.EmoteItem
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.get

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    state: ChatViewModel.State,
    chatPreferencesRepository: ChatPreferencesRepository = get(),
    onEmoteClick: (Emote) -> Unit,
    onChatterClick: (Chatter) -> Unit,
    onMessageChange: (TextFieldValue) -> Unit,
    onToggleEmotePicker: () -> Unit,
    onClearReplyingTo: () -> Unit,
    onSubmit: () -> Unit
) {
    val animateEmotes by chatPreferencesRepository.animateEmotes.collectAsState(initial = true)

    when (state) {
        ChatViewModel.State.Initial -> {}
        is ChatViewModel.State.Chatting -> {
            Surface(
                shadowElevation = 2.dp,
                tonalElevation = 1.dp
            ) {
                ChatInput(
                    modifier = modifier,
                    message = state.inputMessage,
                    previousWord = state.previousWord,
                    emotes = state.allEmotes,
                    chatters = state.chatters,
                    replyingTo = state.replyingTo,
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

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    message: TextFieldValue,
    previousWord: CharSequence,
    emotes: ImmutableSet<Emote>,
    chatters: ImmutableSet<Chatter>,
    animateEmotes: Boolean,
    replyingTo: ChatEntry?,
    onEmoteClick: (Emote) -> Unit,
    onChatterClick: (Chatter) -> Unit,
    onMessageChange: (TextFieldValue) -> Unit,
    onToggleEmotePicker: () -> Unit,
    onClearReplyingTo: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(modifier = modifier) {
        AnimatedVisibility(visible = replyingTo?.data != null) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InReplyToMessage(
                    modifier = Modifier.weight(1f),
                    userName = replyingTo?.data?.userName.orEmpty(),
                    message = replyingTo?.data?.message.orEmpty()
                )

                IconButton(onClick = onClearReplyingTo) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Stop replying to this message"
                    )
                }
            }
        }

        Row(
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
                FloatingActionButton(onClick = onSubmit) {
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
        singleLine = true,
        onValueChange = onMessageChange,
        shape = FloatingActionButtonDefaults.shape,
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
            IconButton(onClick = onToggleEmotePicker) {
                Icon(
                    Icons.Default.Mood,
                    contentDescription = stringResource(R.string.chat_input_emote_cd)
                )
            }
        },
        trailingIcon = {
            if (message.text.isNotEmpty()) {
                IconButton(
                    onClick = { onMessageChange(TextFieldValue("")) }
                ) {
                    Icon(
                        Icons.Default.Clear,
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
                    onClick = { onEmoteClick(emote) }
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
