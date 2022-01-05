package com.github.andreyasadchy.xtra.ui.view.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.MultiAutoCompleteTextView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.*
import com.github.andreyasadchy.xtra.ui.common.ChatAdapter
import com.github.andreyasadchy.xtra.util.*
import com.github.andreyasadchy.xtra.util.chat.Command
import com.github.andreyasadchy.xtra.util.chat.RoomState
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.auto_complete_emotes_list_item.view.*
import kotlinx.android.synthetic.main.view_chat.view.*
import kotlin.math.max
import com.github.andreyasadchy.xtra.model.helix.emote.Emote as TwitchEmote

var MAX_ADAPTER_COUNT = 200
var emoteQuality = "3"
var stvQuality = 4

class ChatView : ConstraintLayout {

    interface MessageSenderCallback {
        fun send(message: CharSequence)
        fun addEmoteSets(clientId: String?, userToken: String?)
    }

    private lateinit var adapter: ChatAdapter

    private var isChatTouched = false
    private var showFlexbox = false

    private var hasRecentEmotes: Boolean? = null
    private var emotesAddedCount = 0

    private var autoCompleteList: MutableList<Any>? = null
    private var autoCompleteAdapter: AutoCompleteAdapter? = null

    private lateinit var fragment: Fragment
    private var messagingEnabled = false

    private var messageCallback: MessageSenderCallback? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_chat, this)
    }

    fun init(fragment: Fragment) {
        this.fragment = fragment
        emoteQuality = context.prefs().getInt(C.CHAT_EMOTEQUALITY, 3).toString()
        stvQuality = context.prefs().getInt(C.CHAT_STVQUALITY, 4)
        MAX_ADAPTER_COUNT = context.prefs().getInt(C.CHAT_LIMIT, 200)
        adapter = ChatAdapter(fragment, context.convertDpToPixels(29.5f), context.convertDpToPixels(18.5f), context.prefs().getBoolean(C.CHAT_RANDOMCOLOR, true),
            context.prefs().getBoolean(C.CHAT_BOLDNAMES, false), context.prefs().getInt(C.CHAT_BADGEQUALITY, 3), context.prefs().getBoolean(C.ANIMATED_EMOTES, true),
            context.prefs().getBoolean(C.CHAT_ZEROWIDTH, true))
        recyclerView.let {
            it.adapter = adapter
            it.itemAnimator = null
            it.layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isChatTouched = newState == RecyclerView.SCROLL_STATE_DRAGGING
                    btnDown.isVisible = shouldShowButton()
                    if (showFlexbox && flexbox.isGone) {
                        flexbox.visible()
                        flexbox.postDelayed({ flexbox.gone() }, 5000)
                    }
                }
            })
        }
        btnDown.setOnClickListener {
            post {
                recyclerView.scrollToPosition(adapter.messages!!.lastIndex)
                it.toggleVisibility()
            }
        }
    }

    fun submitList(list: MutableList<ChatMessage>) {
        adapter.messages = list
    }

    fun notifyMessageAdded() {
        adapter.messages!!.apply {
            adapter.notifyItemInserted(lastIndex)
            if (size >= MAX_ADAPTER_COUNT + 1) {
                val removeCount = size - MAX_ADAPTER_COUNT + 1
                repeat(removeCount) {
                    removeAt(0)
                }
                adapter.notifyItemRangeRemoved(0, removeCount)
            }
            if (!isChatTouched && btnDown.isGone) {
                recyclerView.scrollToPosition(lastIndex)
            }
        }
    }

    fun notifyRoomState(roomState: RoomState) {
        if (roomState.emote != null) {
            when (roomState.emote) {
                "0" -> text_emote.gone()
                "1" -> text_emote.visible()
            }
        }
        if (roomState.followers != null) {
            when (roomState.followers) {
                "-1" -> text_followers.gone()
                "0" -> {
                    text_followers.text = context.getString(R.string.room_followers)
                    text_followers.visible()
                }
                else -> {
                    text_followers.text = context.getString(R.string.room_followers_min, roomState.followers)
                    text_followers.visible()
                }
            }
        }
        if (roomState.unique != null) {
            when (roomState.unique) {
                "0" -> text_unique.gone()
                "1" -> text_unique.visible()
            }
        }
        if (roomState.slow != null) {
            when (roomState.slow) {
                "0" -> text_slow.gone()
                else -> {
                    text_slow.text = context.getString(R.string.room_slow, roomState.slow)
                    text_slow.visible()
                }
            }
        }
        if (roomState.subs != null) {
            when (roomState.subs) {
                "0" -> text_subs.gone()
                "1" -> text_subs.visible()
            }
        }
        if (text_emote.isGone && text_followers.isGone && text_unique.isGone && text_slow.isGone && text_subs.isGone) {
            showFlexbox = false
            flexbox.gone()
        } else {
            showFlexbox = true
            flexbox.visible()
            flexbox.postDelayed({ flexbox.gone() }, 5000)
        }
    }

    fun notifyCommand(command: Command) {
        val message = when (command.type) {
            "join" -> context.getString(R.string.chat_join, command.message)
            "clearmsg" -> context.getString(R.string.chat_clearmsg, command.message, command.duration)
            "clearchat" -> context.getString(R.string.chat_clear)
            "timeout" -> context.getString(R.string.chat_timeout, command.message, TwitchApiHelper.getDurationFromSeconds(command.duration))
            "ban" -> context.getString(R.string.chat_ban, command.message)
            else -> command.message
        }
        adapter.messages?.add(LiveChatMessage(message = message, color = "#999999", isAction = true, emotes = command.emotes))
        notifyMessageAdded()
    }

    fun addEmotes(list: List<Emote>) {
        when (list.firstOrNull()) {
            is BttvEmote, is FfzEmote, is StvEmote -> {
                adapter.addEmotes(list)
                if (messagingEnabled) {
                    autoCompleteList!!.addAll(list)
                }
            }
            is TwitchEmote -> {
                if (messagingEnabled) {
                    autoCompleteList!!.addAll(list)
                }
            }
            is RecentEmote -> hasRecentEmotes = true
        }
        if (messagingEnabled && ++emotesAddedCount == 3) { //TODO refactor to not wait
            autoCompleteAdapter = AutoCompleteAdapter(context, fragment, autoCompleteList!!).apply {
                setNotifyOnChange(false)
                editText.setAdapter(this)

                var previousSize = 0
                editText.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus && count != previousSize) {
                        previousSize = count
                        notifyDataSetChanged()
                    }
                    setNotifyOnChange(hasFocus)
                }
            }
        }
    }

    fun setUsername(username: String) {
        adapter.setUsername(username)
    }

    fun setChatters(chatters: Collection<Chatter>) {
        autoCompleteList = chatters.toMutableList()
    }

    fun addChatter(chatter: Chatter) {
        autoCompleteAdapter?.add(chatter)
    }

    fun setCallback(callback: MessageSenderCallback) {
        messageCallback = callback
    }

    fun hideEmotesMenu(): Boolean {
        return if (viewPager.isVisible) {
            viewPager.gone()
            true
        } else {
            false
        }
    }

    fun appendEmote(emote: Emote) {
        editText.text.append(emote.name).append(' ')
    }

    @SuppressLint("SetTextI18n")
    fun reply(userName: CharSequence) {
        val text = "@$userName "
        editText.apply {
            setText(text)
            setSelection(text.length)
            showKeyboard()
        }
    }

    fun setMessage(text: CharSequence) {
        editText.setText(text)
    }

    fun enableChatInteraction(enableMessaging: Boolean) {
        adapter.setOnClickListener { original, formatted, login ->
            editText.hideKeyboard()
            editText.clearFocus()
            MessageClickedDialog.newInstance(enableMessaging, original, formatted, login).show(fragment.childFragmentManager, null)
        }
        if (enableMessaging) {
            editText.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                if (text?.isNotBlank() == true) {
                    send.visible()
                    clear.visible()
                } else {
                    send.gone()
                    clear.gone()
                }
            })
            editText.setTokenizer(SpaceTokenizer())
            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendMessage()
                } else {
                    false
                }
            }
            clear.setOnClickListener {
                val text = editText.text.toString().trimEnd()
                editText.setText(text.substring(0, max(text.lastIndexOf(' '), 0)))
                editText.setSelection(editText.length())
            }
            clear.setOnLongClickListener {
                editText.text.clear()
                true
            }
            send.setOnClickListener { sendMessage() }
            messageView.visible()
            viewPager.adapter = object : FragmentStatePagerAdapter(fragment.childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

                override fun getItem(position: Int): Fragment {
                    return EmotesFragment.newInstance(position)
                }

                override fun getCount(): Int = 3

                override fun getPageTitle(position: Int): CharSequence? {
                    return when (position) {
                        0 -> context.getString(R.string.recent_emotes)
                        1 -> "Twitch"
                        else -> "BTTV/FFZ/7TV"
                    }
                }
            }
            viewPager.offscreenPageLimit = 2
            emotes.setOnClickListener {
                //TODO add animation
                with(viewPager) {
                    if (isGone) {
                        if (hasRecentEmotes != true && currentItem == 0) {
                            setCurrentItem(1, false)
                        }
                        visible()
                        if (currentItem == 1) {
                            messageCallback?.addEmoteSets(context.prefs().getString(C.HELIX_CLIENT_ID, ""), context.prefs().getString(C.TOKEN, ""))
                        }
                    } else {
                        gone()
                    }
                }
            }
            messagingEnabled = true
        }
    }

    override fun onDetachedFromWindow() {
        recyclerView.adapter = null
        super.onDetachedFromWindow()
    }

    private fun sendMessage(): Boolean {
        editText.hideKeyboard()
        editText.clearFocus()
        hideEmotesMenu()
        return messageCallback?.let {
            val text = editText.text.trim()
            editText.text.clear()
            if (text.isNotEmpty()) {
                it.send(text)
                true
            } else {
                false
            }
        } == true
    }

    private fun shouldShowButton(): Boolean {
        val offset = recyclerView.computeVerticalScrollOffset()
        if (offset < 0) {
            return false
        }
        val extent = recyclerView.computeVerticalScrollExtent()
        val range = recyclerView.computeVerticalScrollRange()
        val percentage = (100f * offset / (range - extent).toFloat())
        return percentage < 97f
    }

    class SpaceTokenizer : MultiAutoCompleteTextView.Tokenizer {

        override fun findTokenStart(text: CharSequence, cursor: Int): Int {
            var i = cursor

            while (i > 0 && text[i - 1] != ' ') {
                i--
            }
            while (i < cursor && text[i] == ' ') {
                i++
            }

            return i
        }

        override fun findTokenEnd(text: CharSequence, cursor: Int): Int {
            var i = cursor
            val len = text.length

            while (i < len) {
                if (text[i] == ' ') {
                    return i
                } else {
                    i++
                }
            }

            return len
        }

        override fun terminateToken(text: CharSequence): CharSequence {
            return "${if (text.startsWith(':')) text.substring(1) else text} "
        }
    }

    class AutoCompleteAdapter(
            context: Context,
            private val fragment: Fragment,
            list: List<Any>) : ArrayAdapter<Any>(context, 0, list) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewHolder: ViewHolder

            val item = getItem(position)!!
            return when (getItemViewType(position)) {
                TYPE_EMOTE -> {
                    if (convertView == null) {
                        val view = LayoutInflater.from(context).inflate(R.layout.auto_complete_emotes_list_item, parent, false)
                        viewHolder = ViewHolder(view).also { view.tag = it }
                    } else {
                        viewHolder = convertView.tag as ViewHolder
                    }
                    viewHolder.containerView.apply {
                        item as Emote
                        image.loadImage(fragment, item.url, diskCacheStrategy = DiskCacheStrategy.DATA)
                        name.text = item.name
                    }
                }
                else -> {
                    if (convertView == null) {
                        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
                        viewHolder = ViewHolder(view).also { view.tag = it }
                    } else {
                        viewHolder = convertView.tag as ViewHolder
                    }
                    (viewHolder.containerView as TextView).apply {
                        text = (item as Chatter).name
                    }
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (getItem(position) is Emote) TYPE_EMOTE else TYPE_USERNAME
        }

        override fun getViewTypeCount(): Int = 2

        class ViewHolder(override val containerView: View) : LayoutContainer

        private companion object {
            const val TYPE_EMOTE = 0
            const val TYPE_USERNAME = 1
        }
    }
}
