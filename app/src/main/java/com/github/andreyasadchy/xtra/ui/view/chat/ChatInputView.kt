package com.github.andreyasadchy.xtra.ui.view.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.MultiAutoCompleteTextView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.BttvEmote
import com.github.andreyasadchy.xtra.model.chat.Chatter
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.FfzEmote
import com.github.andreyasadchy.xtra.model.chat.RecentEmote
import com.github.andreyasadchy.xtra.model.chat.StvEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.chat.RoomState
import com.github.andreyasadchy.xtra.util.hideKeyboard
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.showKeyboard
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.auto_complete_emotes_list_item.view.image
import kotlinx.android.synthetic.main.auto_complete_emotes_list_item.view.name
import kotlinx.android.synthetic.main.view_chat_input.view.clear
import kotlinx.android.synthetic.main.view_chat_input.view.editText
import kotlinx.android.synthetic.main.view_chat_input.view.flexboxChatMode
import kotlinx.android.synthetic.main.view_chat_input.view.messageView
import kotlinx.android.synthetic.main.view_chat_input.view.send
import kotlinx.android.synthetic.main.view_chat_input.view.textEmote
import kotlinx.android.synthetic.main.view_chat_input.view.textFollowers
import kotlinx.android.synthetic.main.view_chat_input.view.textInputLayoutChat
import kotlinx.android.synthetic.main.view_chat_input.view.textSlow
import kotlinx.android.synthetic.main.view_chat_input.view.textSubs
import kotlinx.android.synthetic.main.view_chat_input.view.textUnique
import kotlinx.android.synthetic.main.view_chat_input.view.viewPager
import kotlin.math.max

class ChatInputView : LinearLayout {

    fun interface OnMessageSendListener {
        fun send(message: CharSequence)
    }

    private var hasRecentEmotes: Boolean? = null
    private var emotesAddedCount = 0

    private var autoCompleteList: MutableList<Any>? = null
    private var autoCompleteAdapter: AutoCompleteAdapter? = null

    private lateinit var fragment: Fragment
    private var messagingEnabled = false

    private var messageCallback: OnMessageSendListener? = null

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
        View.inflate(context, R.layout.view_chat_input, this)
        orientation = VERTICAL
    }

    fun init(fragment: Fragment) {
        this.fragment = fragment
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
            val navBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            messageView.setPadding(
                messageView.paddingLeft,
                messageView.paddingTop,
                messageView.paddingRight,
                if (imeInsets.bottom > 0) imeInsets.bottom else navBarInsets.bottom
            )

            if (imeInsets.bottom > 0) {
                // Hide emote picker when keyboard is opened
                hideEmotesMenu()
            }

            windowInsets
        }
    }

    fun addEmotes(list: List<Emote>) {
        when (list.firstOrNull()) {
            is BttvEmote, is FfzEmote, is StvEmote -> {
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

        if (messagingEnabled && ++emotesAddedCount == 3) {
            // TODO refactor to not wait
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

    fun setChatters(chatters: Collection<Chatter>) {
        autoCompleteList = chatters.toMutableList()
    }

    fun addChatter(chatter: Chatter) {
        autoCompleteAdapter?.add(chatter)
    }

    fun setOnMessageSendListener(callback: OnMessageSendListener) {
        messageCallback = callback
    }

    fun hideEmotesMenu(): Boolean {
        return if (viewPager.isVisible) {
            viewPager.isVisible = false
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

    fun notifyRoomState(roomState: RoomState) {
        if (roomState.emote != null) {
            when (roomState.emote) {
                "0" -> textEmote.isVisible = false
                "1" -> textEmote.isVisible = true
            }
        } else {
            textEmote.isVisible = false
        }

        if (roomState.followers != null) {
            when (roomState.followers) {
                "-1" -> textFollowers.isVisible = false
                "0" -> {
                    textFollowers.text = context.getString(R.string.room_followers)
                    textFollowers.isVisible = true
                }
                else -> {
                    textFollowers.text = context.getString(
                        R.string.room_followers_min,
                        TwitchApiHelper.getDurationFromSeconds(
                            context,
                            (roomState.followers.toInt() * 60).toString()
                        )
                    )
                    textFollowers.isVisible = true
                }
            }
        } else {
            textFollowers.isVisible = false
        }

        if (roomState.unique != null) {
            when (roomState.unique) {
                "0" -> textUnique.isVisible = false
                "1" -> textUnique.isVisible = true
            }
        } else {
            textUnique.isVisible = false
        }

        if (roomState.slow != null) {
            when (roomState.slow) {
                "0" -> textSlow.isVisible = false
                else -> {
                    textSlow.text = context.getString(
                        R.string.room_slow,
                        TwitchApiHelper.getDurationFromSeconds(context, roomState.slow)
                    )
                    textSlow.isVisible = true
                }
            }
        } else {
            textSlow.isVisible = false
        }

        if (roomState.subs != null) {
            when (roomState.subs) {
                "0" -> textSubs.isVisible = false
                "1" -> textSubs.isVisible = true
            }
        } else {
            textSubs.isVisible = false
        }

        if (textEmote.isGone && textFollowers.isGone && textUnique.isGone && textSlow.isGone && textSubs.isGone) {
            flexboxChatMode.isVisible = false
        } else {
            flexboxChatMode.isVisible = true
        }
    }

    fun enableChatInteraction(enableMessaging: Boolean) {
        if (enableMessaging) {
            editText.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                if (text?.isNotBlank() == true) {
                    send.isVisible = true
                    clear.isVisible = true
                } else {
                    send.isVisible = false
                    clear.isVisible = false
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

            textInputLayoutChat.setEndIconOnClickListener {
                // TODO add animation
                if (viewPager.isGone) {
                    if (hasRecentEmotes != true && viewPager.currentItem == 0) {
                        viewPager.setCurrentItem(1, false)
                    }
                    editText.hideKeyboard()
                    viewPager.isVisible = true
                } else {
                    editText.showKeyboard()
                    viewPager.isVisible = false
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
            messageView.isVisible = true

            viewPager.adapter = object : FragmentStatePagerAdapter(
                fragment.childFragmentManager,
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
            ) {
                override fun getItem(position: Int): Fragment {
                    return EmotesFragment.newInstance(position)
                }

                override fun getCount(): Int = 3

                override fun getPageTitle(position: Int): CharSequence? {
                    return when (position) {
                        0 -> context.getString(R.string.recent_emotes)
                        1 -> "Twitch"
                        else -> "7TV/BTTV/FFZ"
                    }
                }
            }

            viewPager.offscreenPageLimit = 2

            messagingEnabled = true
        }
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
        list: List<Any>
    ) : ArrayAdapter<Any>(context, 0, list) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewHolder: ViewHolder

            val item = getItem(position)!!
            return when (getItemViewType(position)) {
                TYPE_EMOTE -> {
                    if (convertView == null) {
                        val view = LayoutInflater.from(context)
                            .inflate(R.layout.auto_complete_emotes_list_item, parent, false)
                        viewHolder = ViewHolder(view).also { view.tag = it }
                    } else {
                        viewHolder = convertView.tag as ViewHolder
                    }
                    viewHolder.containerView.apply {
                        item as Emote
                        image.loadImage(
                            fragment,
                            item.url,
                            diskCacheStrategy = DiskCacheStrategy.DATA
                        )
                        name.text = item.name
                    }
                }
                else -> {
                    if (convertView == null) {
                        val view = LayoutInflater.from(context)
                            .inflate(android.R.layout.simple_list_item_1, parent, false)
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
