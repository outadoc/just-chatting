package com.github.andreyasadchy.xtra.ui.view.chat

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.FfzEmote
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.PubSubPointReward
import com.github.andreyasadchy.xtra.model.chat.RecentEmote
import com.github.andreyasadchy.xtra.model.chat.StvEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchBadge
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.ui.common.ChatAdapter
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.chat.Command
import com.github.andreyasadchy.xtra.util.convertDpToPixels
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.view_chat.view.btnDown
import kotlinx.android.synthetic.main.view_chat.view.recyclerView
import java.util.Locale

class ChatView : ConstraintLayout {

    companion object {
        var MAX_ADAPTER_COUNT = 200
        var MAX_LIST_COUNT = MAX_ADAPTER_COUNT + 1
        var emoteQuality = "4"
        var animateGifs = true
    }

    private lateinit var adapter: ChatAdapter

    private var isChatTouched = false
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
    }

    fun init() {
        emoteQuality = context.prefs().getString(C.CHAT_IMAGE_QUALITY, "4") ?: "4"
        animateGifs = context.prefs().getBoolean(C.ANIMATED_EMOTES, true)
        MAX_ADAPTER_COUNT = context.prefs().getInt(C.CHAT_LIMIT, 200)

        adapter = ChatAdapter(
            context = context,
            emoteSize = context.convertDpToPixels(29.5f),
            badgeSize = context.convertDpToPixels(18.5f),
            pickRandomColors = context.prefs().getBoolean(C.CHAT_RANDOMCOLOR, true),
            enableTimestamps = context.prefs().getBoolean(C.CHAT_TIMESTAMPS, false),
            timestampFormat = context.prefs().getString(C.CHAT_TIMESTAMP_FORMAT, "0"),
            firstMsgVisibility = context.prefs().getString(C.CHAT_FIRSTMSG_VISIBILITY, "0"),
            firstChatMsg = context.getString(R.string.chat_first),
            rewardChatMsg = context.getString(R.string.chat_reward),
            redeemedChatMsg = context.getString(R.string.redeemed),
            redeemedNoMsg = context.getString(R.string.user_redeemed),
            emoteQuality = emoteQuality,
            animateGifs = animateGifs
        )

        adapter.setOnClickListener { original, formatted, userId, fullMsg ->
            messageClickListener?.send(original, formatted, userId, fullMsg)
        }

        recyclerView.let {
            it.adapter = adapter
            it.itemAnimator = null
            it.layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }

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

            if (size >= MAX_LIST_COUNT) {
                val removeCount = size - MAX_ADAPTER_COUNT
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
        val lang = Locale.getDefault().language
        val message = when (command.type) {
            "join" -> context.getString(R.string.chat_join, command.message)
            "disconnect" -> context.getString(
                R.string.chat_disconnect,
                command.message,
                command.duration
            )
            "disconnect_command" -> {
                adapter.messages?.clear()
                context.getString(R.string.disconnected)
            }
            "send_msg_error" -> context.getString(R.string.chat_send_msg_error, command.message)
            "socket_error" -> context.getString(R.string.chat_socket_error, command.message)
            "notice" -> if (lang == "ar" || lang == "es" || lang == "ja" || lang == "pt" || lang == "ru" || lang == "tr") {
                TwitchApiHelper.getNoticeString(context, command.duration, command.message)
                    ?: command.message
            } else {
                command.message
            }
            "clearmsg" -> context.getString(
                R.string.chat_clearmsg,
                command.message,
                command.duration
            )
            "clearchat" -> context.getString(R.string.chat_clear)
            "timeout" -> context.getString(
                R.string.chat_timeout,
                command.message,
                TwitchApiHelper.getDurationFromSeconds(context, command.duration)
            )
            "ban" -> context.getString(R.string.chat_ban, command.message)
            else -> command.message
        }

        adapter.messages?.add(
            LiveChatMessage(
                message = message,
                color = "#999999",
                isAction = true,
                emotes = command.emotes,
                timestamp = command.timestamp,
                fullMsg = command.fullMsg
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

    fun addRecentMessages(list: List<LiveChatMessage>) {
        adapter.messages?.addAll(0, list)
        scrollToBottom()
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
