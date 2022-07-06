package com.github.andreyasadchy.xtra.ui.view.chat

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.BttvEmote
import com.github.andreyasadchy.xtra.model.chat.ChatMessage
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.Command
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.FfzEmote
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.PubSubPointReward
import com.github.andreyasadchy.xtra.model.chat.RecentEmote
import com.github.andreyasadchy.xtra.model.chat.RoomState
import com.github.andreyasadchy.xtra.model.chat.StvEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchBadge
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.ui.common.ChatAdapter
import com.github.andreyasadchy.xtra.ui.view.AlternatingBackgroundItemDecoration
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.view_chat.view.*
import kotlin.time.Duration

class ChatView : LinearLayout {

    private lateinit var adapter: ChatAdapter

    private var isChatTouched = false
    private var maxAdapterCount: Int = -1
    private var hasRecentEmotes: Boolean? = null

    private var messageClickListener: OnMessageClickListener? = null

    private val rewardList = mutableListOf<Pair<LiveChatMessage?, PubSubPointReward?>>()

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_chat, this)
        orientation = VERTICAL
    }

    fun init(maxAdapterCount: Int) {
        this.maxAdapterCount = maxAdapterCount

        adapter = ChatAdapter(
            context = context,
            enableTimestamps = context.prefs().getBoolean(C.CHAT_TIMESTAMPS, false),
            animateEmotes = context.prefs().getBoolean(C.ANIMATED_EMOTES, true)
        )

        adapter.setOnClickListener { original, formatted, userId ->
            messageClickListener?.send(original, formatted, userId)
        }

        recyclerView.let {
            it.adapter = adapter
            it.itemAnimator = null
            it.layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }

            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.colorSurfaceVariant, typedValue, true)
            val altBackground = ColorUtils.setAlphaComponent(typedValue.data, 40)

            it.addItemDecoration(
                AlternatingBackgroundItemDecoration(
                    oddBackground = Color.TRANSPARENT,
                    evenBackground = altBackground
                )
            )

            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isChatTouched = newState == RecyclerView.SCROLL_STATE_DRAGGING
                    btnDown.isVisible = shouldShowButton()
                }
            })
        }

        btnDown.setOnClickListener {
            post {
                scrollToBottom()
                it.isVisible = !it.isVisible
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
            val navBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            btnDown.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = topMargin + navBarInsets.bottom
            }
            windowInsets
        }
    }

    fun submitList(list: MutableList<ChatMessage>) {
        adapter.messages = list
    }

    fun notifyMessageAdded() {
        adapter.messages?.apply {
            adapter.notifyItemInserted(lastIndex)

            val size = size
            if (size > maxAdapterCount) {
                val removeCount = size - maxAdapterCount + if (size % 2 == 1) 1 else 0
                repeat(removeCount) {
                    removeAt(0)
                }
                adapter.notifyItemRangeRemoved(0, removeCount)
            }

            if (!isChatTouched && btnDown.isGone) {
                scrollToBottom()
            }
        }
    }

    fun setOnMessageClickListener(callback: OnMessageClickListener) {
        messageClickListener = callback
    }

    fun scrollToBottom() {
        adapter.messages?.let { messages ->
            recyclerView.scrollToPosition(messages.lastIndex)
        }
    }

    fun notifyEmotesLoaded(loaded: Boolean) {
        adapter.messages?.size?.let { messageCount ->
            adapter.notifyItemRangeChanged(messageCount - 40, 40)
        }
    }

    fun notifyCommand(command: Command) {
        val message = when (command) {
            is Command.ClearChat -> context.getString(R.string.chat_clear)
            is Command.ClearMessage ->
                context.getString(
                    R.string.chat_clearmsg,
                    command.message,
                    command.duration
                )
            is Command.Join ->
                context.getString(R.string.chat_join, command.message)
            is Command.Disconnect ->
                context.getString(
                    R.string.chat_disconnect,
                    command.message,
                    command.duration
                )
            is Command.Notice ->
                TwitchApiHelper.getNoticeString(
                    context = context,
                    msgId = command.duration,
                    message = command.message
                ) ?: command.message
            is Command.SendMessageError ->
                context.getString(
                    R.string.chat_send_msg_error,
                    command.message
                )
            is Command.SocketError ->
                context.getString(R.string.chat_socket_error, command.message)
            is Command.Ban -> context.getString(R.string.chat_ban, command.message)
            is Command.Timeout -> context.getString(
                R.string.chat_timeout,
                command.message,
                command.duration
            )
            is Command.UserNotice -> command.message
        }

        adapter.messages?.add(
            LiveChatMessage(
                message = message,
                isAction = true,
                emotes = command.emotes,
                timestamp = command.timestamp
            )
        )

        notifyMessageAdded()
    }

    fun notifyReward(message: ChatMessage) {
        when (message) {
            is LiveChatMessage -> {
                val item = rewardList.find {
                    it.second?.id == message.rewardId && it.second?.userId == message.userId
                }

                if (item != null) {
                    message.apply { pointReward = item.second }.let {
                        rewardList.remove(item)
                        adapter.messages?.add(it)
                        notifyMessageAdded()
                    }
                } else {
                    rewardList.add(Pair(message, null))
                }
            }
            is PubSubPointReward -> {
                val item = rewardList.find {
                    it.first?.rewardId == message.id && it.first?.userId == message.userId
                }

                if (item != null) {
                    item.first?.apply { pointReward = message }?.let {
                        rewardList.remove(item)
                        adapter.messages?.add(it)
                        notifyMessageAdded()
                    }
                } else {
                    rewardList.add(Pair(null, message))
                }
            }
        }
    }

    fun notifyRoomState(roomState: RoomState) {
        textEmote.isVisible = roomState.emote

        if (roomState.followers != null) {
            when (roomState.followers) {
                Duration.ZERO -> {
                    textFollowers.text = context.getString(R.string.room_followers)
                    textFollowers.isVisible = true
                }
                else -> {
                    textFollowers.text = context.getString(
                        R.string.room_followers_min,
                        roomState.followers.toString()
                    )
                    textFollowers.isVisible = true
                }
            }
        } else {
            textFollowers.isVisible = false
        }

        textUnique.isVisible = roomState.unique

        if (roomState.slow != null) {
            when (roomState.slow) {
                Duration.ZERO -> textSlow.isVisible = false
                else -> {
                    textSlow.text = context.getString(
                        R.string.room_slow,
                        roomState.slow.toString()
                    )
                    textSlow.isVisible = true
                }
            }
        } else {
            textSlow.isVisible = false
        }

        textSubs.isVisible = roomState.subs

        flexboxChatMode.isVisible =
            !textEmote.isGone ||
            !textFollowers.isGone ||
            !textUnique.isGone ||
            !textSlow.isGone ||
            !textSubs.isGone
    }

    fun addGlobalBadges(list: List<TwitchBadge>?) {
        if (list != null) {
            adapter.addGlobalBadges(list)
        }
    }

    fun addChannelBadges(list: List<TwitchBadge>) {
        adapter.addChannelBadges(list)
    }

    fun addCheerEmotes(list: List<CheerEmote>) {
        adapter.addCheerEmotes(list)
    }

    fun addEmotes(list: List<Emote>) {
        when (list.firstOrNull()) {
            is BttvEmote, is FfzEmote, is StvEmote -> {
                adapter.addEmotes(list)
            }
            is TwitchEmote -> {}
            is RecentEmote -> hasRecentEmotes = true
        }
    }

    fun setUsername(username: String) {
        adapter.setUsername(username)
    }

    override fun onDetachedFromWindow() {
        recyclerView.adapter = null
        super.onDetachedFromWindow()
    }

    private fun shouldShowButton(): Boolean {
        val offset = recyclerView.computeVerticalScrollOffset()
        if (offset < 0) return false

        val extent = recyclerView.computeVerticalScrollExtent()
        val range = recyclerView.computeVerticalScrollRange()
        val percentage = (100f * offset / (range - extent).toFloat())
        return percentage < 100f
    }
}
