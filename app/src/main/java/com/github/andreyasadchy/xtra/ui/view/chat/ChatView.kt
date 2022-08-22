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
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
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
import com.github.andreyasadchy.xtra.ui.view.chat.model.ChatEntry
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.view_chat.view.btnDown
import kotlinx.android.synthetic.main.view_chat.view.flexboxChatMode
import kotlinx.android.synthetic.main.view_chat.view.recyclerView
import kotlinx.android.synthetic.main.view_chat.view.textEmote
import kotlinx.android.synthetic.main.view_chat.view.textFollowers
import kotlinx.android.synthetic.main.view_chat.view.textSlow
import kotlinx.android.synthetic.main.view_chat.view.textSubs
import kotlinx.android.synthetic.main.view_chat.view.textUnique
import kotlin.time.Duration

class ChatView : LinearLayout {

    private lateinit var adapter: ChatAdapter

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
            it.layoutManager = object : LinearLayoutManager(context) {

                init {
                    stackFromEnd = true
                }

                private var isChatTouched = false

                override fun onScrollStateChanged(state: Int) {
                    super.onScrollStateChanged(state)
                    isChatTouched = state != RecyclerView.SCROLL_STATE_IDLE
                    btnDown.isVisible = shouldShowButton()
                }

                override fun onLayoutCompleted(state: RecyclerView.State?) {
                    super.onLayoutCompleted(state)
                    state ?: return

                    if (!isChatTouched && btnDown.isGone) {
                        scrollToPosition(state.itemCount - 1)
                    }
                }
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

    fun submitList(list: List<ChatEntry>) {
        adapter.submitList(list)
    }

    fun setOnMessageClickListener(callback: OnMessageClickListener) {
        messageClickListener = callback
    }

    fun scrollToBottom() {
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    fun notifyEmotesLoaded(loaded: Boolean) {
        adapter.itemCount.let { messageCount ->
            adapter.notifyItemRangeChanged(messageCount - 40, 40)
        }
    }

    fun notifyRoomState(roomState: RoomState) {
        textEmote.isVisible = roomState.isEmoteOnly

        if (roomState.minFollowDuration != null) {
            when (roomState.minFollowDuration) {
                Duration.ZERO -> {
                    textFollowers.text = context.getString(R.string.room_followers)
                    textFollowers.isVisible = true
                }
                else -> {
                    textFollowers.text = context.getString(
                        R.string.room_followers_min,
                        roomState.minFollowDuration.toString()
                    )
                    textFollowers.isVisible = true
                }
            }
        } else {
            textFollowers.isVisible = false
        }

        textUnique.isVisible = roomState.uniqueMessagesOnly

        if (roomState.slowModeDuration != null) {
            textSlow.text = context.getString(
                R.string.room_slow,
                roomState.slowModeDuration.toString()
            )
            textSlow.isVisible = true
        } else {
            textSlow.isVisible = false
        }

        textSubs.isVisible = roomState.isSubOnly

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
        val ratio = offset / (range - extent)
        return ratio < 1f
    }
}
