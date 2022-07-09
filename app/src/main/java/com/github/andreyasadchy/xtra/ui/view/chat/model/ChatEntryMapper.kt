package com.github.andreyasadchy.xtra.ui.view.chat.model

import android.content.Context
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.ChatCommand
import com.github.andreyasadchy.xtra.model.chat.Command
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.PingCommand
import com.github.andreyasadchy.xtra.model.chat.PubSubPointReward
import com.github.andreyasadchy.xtra.model.chat.RoomState
import com.github.andreyasadchy.xtra.model.chat.UserState
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import javax.inject.Inject

class ChatEntryMapper @Inject constructor(private val context: Context) {

    fun map(chatCommand: ChatCommand): ChatEntry? {
        return with(chatCommand) {
            when (this) {
                is Command.ClearChat -> {
                    ChatEntry.WithHeader(
                        header = null,
                        data = ChatEntry.Data.Simple(
                            message = context.getString(R.string.chat_clear),
                            timestamp = timestamp
                        )
                    )
                }
                is Command.Ban -> {
                    ChatEntry.WithHeader(
                        header = null,
                        data = ChatEntry.Data.Simple(
                            message = context.getString(R.string.chat_ban, message),
                            timestamp = timestamp
                        )
                    )
                }
                is Command.Timeout -> {
                    ChatEntry.WithHeader(
                        header = null,
                        data = ChatEntry.Data.Simple(
                            message = context.getString(R.string.chat_timeout, message, duration),
                            timestamp = timestamp
                        )
                    )
                }
                is Command.ClearMessage -> {
                    ChatEntry.WithHeader(
                        header = null,
                        data = ChatEntry.Data.Simple(
                            message = context.getString(R.string.chat_clearmsg, message, duration),
                            timestamp = timestamp
                        )
                    )
                }
                is Command.Join -> {
                    ChatEntry.WithHeader(
                        header = null,
                        data = ChatEntry.Data.Simple(
                            message = context.getString(R.string.chat_join, message),
                            timestamp = timestamp
                        )
                    )
                }
                is Command.Disconnect -> {
                    ChatEntry.WithHeader(
                        header = null,
                        data = ChatEntry.Data.Simple(
                            message = context.getString(
                                R.string.chat_disconnect,
                                message,
                                duration
                            ),
                            timestamp = timestamp
                        )
                    )
                }
                is Command.Notice -> {
                    ChatEntry.WithHeader(
                        header = null,
                        data = ChatEntry.Data.Simple(
                            message = TwitchApiHelper.getNoticeString(
                                context = context,
                                msgId = duration,
                                message = message
                            ) ?: message,
                            timestamp = timestamp
                        )
                    )
                }
                is Command.SendMessageError -> {
                    ChatEntry.WithHeader(
                        header = null,
                        data = ChatEntry.Data.Simple(
                            message = context.getString(R.string.chat_send_msg_error, message),
                            timestamp = timestamp
                        )
                    )
                }
                is Command.SocketError -> {
                    ChatEntry.WithHeader(
                        header = null,
                        data = ChatEntry.Data.Simple(
                            message = context.getString(R.string.chat_socket_error, message),
                            timestamp = timestamp
                        )
                    )
                }
                is Command.UserNotice -> {
                    if (userMessage == null) {
                        ChatEntry.WithHeader(
                            header = null,
                            data = ChatEntry.Data.Simple(
                                message = message,
                                timestamp = timestamp
                            )
                        )
                    } else {
                        ChatEntry.WithHeader(
                            header = message ?: msgId?.let { messageId ->
                                TwitchApiHelper.getMessageIdString(context, messageId)
                            },
                            data = ChatEntry.Data.Rich(
                                message = userMessage.message,
                                userId = userMessage.userId,
                                userName = userMessage.userName,
                                userLogin = userMessage.userLogin,
                                isAction = userMessage.isAction,
                                color = userMessage.color,
                                emotes = userMessage.emotes,
                                badges = userMessage.badges,
                                timestamp = timestamp
                            )
                        )
                    }
                }
                is LiveChatMessage -> {
                    val header = when {
                        systemMsg != null -> systemMsg
                        isFirst -> context.getString(R.string.chat_first)
                        rewardId != null -> context.getString(R.string.chat_reward)
                        else -> null
                    }

                    if (header != null || isAction) {
                        ChatEntry.WithHeader(
                            header = header,
                            data = ChatEntry.Data.Rich(
                                message = message,
                                userId = userId,
                                userName = userName,
                                userLogin = userLogin,
                                isAction = isAction,
                                color = color,
                                emotes = emotes,
                                badges = badges,
                                timestamp = timestamp
                            )
                        )
                    } else {
                        ChatEntry.Plain(
                            ChatEntry.Data.Rich(
                                message = message,
                                userId = userId,
                                userName = userName,
                                userLogin = userLogin,
                                isAction = isAction,
                                color = color,
                                emotes = emotes,
                                badges = badges,
                                timestamp = timestamp
                            )
                        )
                    }
                }
                is PubSubPointReward -> {
                    ChatEntry.WithHeader(
                        header = rewardCost?.toString(),
                        headerImage = rewardImage,
                        data = ChatEntry.Data.Simple(
                            message = context.getString(
                                R.string.user_redeemed,
                                userName,
                                rewardTitle
                            ),
                            timestamp = timestamp
                        )
                    )
                }
                PingCommand,
                is RoomState,
                is UserState -> null
            }
        }
    }
}
