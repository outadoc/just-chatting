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
                    ChatEntry.Highlighted(
                        header = context.getString(R.string.chat_clear),
                        data = null,
                        timestamp = timestamp
                    )
                }
                is Command.Ban -> {
                    ChatEntry.Highlighted(
                        header = context.getString(R.string.chat_ban, message),
                        data = null,
                        timestamp = timestamp
                    )
                }
                is Command.Timeout -> {
                    ChatEntry.Highlighted(
                        header = context.getString(R.string.chat_timeout, message, duration),
                        data = null,
                        timestamp = timestamp
                    )
                }
                is Command.ClearMessage -> {
                    ChatEntry.Highlighted(
                        header = context.getString(R.string.chat_clearmsg, message, duration),
                        data = null,
                        timestamp = timestamp
                    )
                }
                is Command.Join -> {
                    ChatEntry.Highlighted(
                        header = context.getString(R.string.chat_join, message),
                        data = null,
                        timestamp = timestamp
                    )
                }
                is Command.Disconnect -> {
                    ChatEntry.Highlighted(
                        header = context.getString(
                            R.string.chat_disconnect,
                            message,
                            duration
                        ),
                        data = null,
                        timestamp = timestamp
                    )
                }
                is Command.Notice -> {
                    ChatEntry.Highlighted(
                        header = TwitchApiHelper.getNoticeString(
                            context = context,
                            msgId = duration,
                            message = message
                        ) ?: message,
                        data = null,
                        timestamp = timestamp
                    )
                }
                is Command.SendMessageError -> {
                    ChatEntry.Highlighted(
                        header = context.getString(R.string.chat_send_msg_error, message),
                        data = null,
                        timestamp = timestamp
                    )
                }
                is Command.SocketError -> {
                    ChatEntry.Highlighted(
                        header = context.getString(R.string.chat_socket_error, message),
                        data = null,
                        timestamp = timestamp
                    )
                }
                is Command.UserNotice -> {
                    if (userMessage == null) {
                        ChatEntry.Highlighted(
                            header = message,
                            data = null,
                            timestamp = timestamp
                        )
                    } else {
                        ChatEntry.Highlighted(
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
                                badges = userMessage.badges
                            ),
                            timestamp = timestamp
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
                        ChatEntry.Highlighted(
                            header = header,
                            data = ChatEntry.Data.Rich(
                                message = message,
                                userId = userId,
                                userName = userName,
                                userLogin = userLogin,
                                isAction = isAction,
                                color = color,
                                emotes = emotes,
                                badges = badges
                            ),
                            timestamp = timestamp
                        )
                    } else {
                        ChatEntry.Simple(
                            ChatEntry.Data.Rich(
                                message = message,
                                userId = userId,
                                userName = userName,
                                userLogin = userLogin,
                                isAction = isAction,
                                color = color,
                                emotes = emotes,
                                badges = badges
                            ),
                            timestamp = timestamp
                        )
                    }
                }
                is PubSubPointReward -> {
                    ChatEntry.Highlighted(
                        header = rewardCost?.toString(),
                        headerImage = rewardImage,
                        data = ChatEntry.Data.Plain(
                            message = context.getString(
                                R.string.user_redeemed,
                                userName,
                                rewardTitle
                            )
                        ),
                        timestamp = timestamp
                    )
                }
                PingCommand,
                is RoomState,
                is UserState -> null
            }
        }
    }
}
