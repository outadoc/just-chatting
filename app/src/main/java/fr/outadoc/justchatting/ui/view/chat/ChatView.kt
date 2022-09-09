package fr.outadoc.justchatting.ui.view.chat

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.ViewChatBinding
import fr.outadoc.justchatting.model.chat.CheerEmote
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.chat.ChatMessage
import fr.outadoc.justchatting.model.chat.PointReward
import fr.outadoc.justchatting.model.chat.RoomState
import fr.outadoc.justchatting.model.chat.TwitchBadge
import fr.outadoc.justchatting.ui.common.ChatAdapter
import fr.outadoc.justchatting.ui.view.AlternatingBackgroundItemDecoration
import fr.outadoc.justchatting.ui.view.chat.model.ChatEntry
import kotlin.time.Duration

class ChatView : LinearLayout {

    private lateinit var adapter: ChatAdapter

    private var messageClickListener: OnMessageClickListener? = null

    private val rewardList = mutableListOf<Pair<ChatMessage?, PointReward?>>()

    private lateinit var viewHolder: ViewChatBinding

    var showTimestamps: Boolean
        get() = adapter.showTimestamps
        set(value) {
            adapter.showTimestamps = value
        }

    var animateEmotes: Boolean
        get() = adapter.animateEmotes
        set(value) {
            adapter.animateEmotes = value
        }

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
        viewHolder = ViewChatBinding.inflate(LayoutInflater.from(context), this)
        orientation = VERTICAL

        adapter = ChatAdapter(context = context)
        adapter.setOnClickListener { original, formatted, userId ->
            messageClickListener?.send(original, formatted, userId)
        }

        viewHolder.recyclerView.let {
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
                    viewHolder.btnDown.isVisible = shouldShowButton()
                }

                override fun onLayoutCompleted(state: RecyclerView.State?) {
                    super.onLayoutCompleted(state)
                    state ?: return

                    if (!isChatTouched && viewHolder.btnDown.isGone) {
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

        viewHolder.btnDown.setOnClickListener {
            post {
                scrollToBottom()
                it.isVisible = !it.isVisible
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
            val navBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            viewHolder.btnDown.updateLayoutParams<MarginLayoutParams> {
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
        viewHolder.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    fun notifyRoomState(roomState: RoomState) = viewHolder.apply {
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

    fun addGlobalBadges(list: List<TwitchBadge>) {
        adapter.addGlobalBadges(list)
    }

    fun addChannelBadges(list: List<TwitchBadge>) {
        adapter.addChannelBadges(list)
    }

    fun addCheerEmotes(list: List<CheerEmote>) {
        adapter.addCheerEmotes(list)
    }

    fun setEmotes(list: Set<Emote>) {
        adapter.addEmotes(list)
    }

    fun setUsername(username: String) {
        adapter.setUsername(username)
    }

    override fun onDetachedFromWindow() {
        viewHolder.recyclerView.adapter = null
        super.onDetachedFromWindow()
    }

    private fun shouldShowButton(): Boolean {
        val offset = viewHolder.recyclerView.computeVerticalScrollOffset()
        if (offset < 0) return false

        val extent = viewHolder.recyclerView.computeVerticalScrollExtent()
        val range = viewHolder.recyclerView.computeVerticalScrollRange()
        val ratio = offset / (range - extent)
        return ratio < 1f
    }
}
