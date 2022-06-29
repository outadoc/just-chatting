package com.github.andreyasadchy.xtra.ui.view.chat

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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.BttvEmote
import com.github.andreyasadchy.xtra.model.chat.Chatter
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.FfzEmote
import com.github.andreyasadchy.xtra.model.chat.RecentEmote
import com.github.andreyasadchy.xtra.model.chat.StvEmote
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.hideKeyboard
import com.github.andreyasadchy.xtra.util.isDarkMode
import com.github.andreyasadchy.xtra.util.loadImage
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.showKeyboard
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.auto_complete_emotes_list_item.view.*
import kotlinx.android.synthetic.main.view_chat_input.view.*

class ChatInputView : LinearLayout {

    fun interface OnMessageSendListener {
        fun send(message: CharSequence)
    }

    private var messagingEnabled = false
    private var hasRecentEmotes: Boolean? = null

    private val autoCompleteAdapter = AutoCompleteAdapter(context)

    private lateinit var childFragmentManager: FragmentManager

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

    fun init(childFragmentManager: FragmentManager) {
        this.childFragmentManager = childFragmentManager

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
                    autoCompleteAdapter.addAll(
                        list.map { AutoCompleteItem.EmoteItem(it) }
                    )
                }
            }
            is TwitchEmote -> {
                if (messagingEnabled) {
                    autoCompleteAdapter.addAll(
                        list.map { AutoCompleteItem.EmoteItem(it) }
                    )
                }
            }
            is RecentEmote -> hasRecentEmotes = true
        }
    }

    fun setChatters(chatters: Collection<Chatter>) {
        autoCompleteAdapter.addAll(
            chatters.map { AutoCompleteItem.UserItem(it) }
        )
    }

    fun addChatter(chatter: Chatter) {
        autoCompleteAdapter.add(AutoCompleteItem.UserItem(chatter))
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
        if (enableMessaging) {
            editText.setAdapter(autoCompleteAdapter)
            editText.setTokenizer(SpaceTokenizer())

            editText.addTextChangedListener(afterTextChanged = { text ->
                send.isVisible = text?.isNotBlank() == true
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendMessage()
                } else {
                    false
                }
            }

            textInputLayoutChat.setStartIconOnClickListener {
                // TODO add animation
                if (viewPager.isGone) {
                    if (hasRecentEmotes != true && viewPager.currentItem == 0) {
                        viewPager.setCurrentItem(1, false)
                    }

                    viewPager.isVisible = true
                    editText.hideKeyboard()
                } else {
                    viewPager.isVisible = false
                    editText.showKeyboard()
                }
            }

            send.setOnClickListener { sendMessage() }
            messageView.isVisible = true

            viewPager.adapter = object : FragmentStatePagerAdapter(
                childFragmentManager,
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
            ) {
                override fun getItem(position: Int): Fragment =
                    EmotesFragment.newInstance(position)

                override fun getCount(): Int = 3

                override fun getPageTitle(position: Int): CharSequence {
                    return when (position) {
                        0 -> context.getString(R.string.emote_tab_recent)
                        1 -> context.getString(R.string.emote_tab_twitch)
                        else -> context.getString(R.string.emote_tab_others)
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
            return "${text.trimStart(':')} "
        }
    }

    class AutoCompleteAdapter(context: Context) : ArrayAdapter<AutoCompleteItem>(context, 0) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val item = getItem(position) ?: error("Invalid item id")
            val viewHolder = when (getItemViewType(position)) {
                TYPE_EMOTE -> {
                    val viewHolder = convertView?.tag as? ViewHolder
                        ?: ViewHolder(
                            LayoutInflater.from(context).inflate(
                                R.layout.auto_complete_emotes_list_item,
                                parent,
                                false
                            )
                        )

                    item as AutoCompleteItem.EmoteItem
                    viewHolder.apply {
                        containerView.tag = this
                        containerView.image.loadImage(
                            context = context,
                            url = item.emote.getUrl(
                                animate = context.prefs().getBoolean(C.ANIMATED_EMOTES, true),
                                screenDensity = context.resources.displayMetrics.density,
                                isDarkTheme = context.isDarkMode
                            )
                        )
                        containerView.name.text = item.emote.name
                    }
                }
                TYPE_USERNAME -> {
                    val viewHolder = convertView?.tag as? ViewHolder
                        ?: ViewHolder(
                            LayoutInflater.from(context).inflate(
                                android.R.layout.simple_list_item_1,
                                parent,
                                false
                            )
                        )

                    item as AutoCompleteItem.UserItem
                    viewHolder.apply {
                        containerView.tag = this
                        (containerView as TextView).text = item.chatter.name
                    }
                }
                else -> error("Invalid item type")
            }

            return viewHolder.containerView
        }

        override fun getItemViewType(position: Int): Int =
            when (getItem(position)) {
                is AutoCompleteItem.EmoteItem -> TYPE_EMOTE
                is AutoCompleteItem.UserItem -> TYPE_USERNAME
                null -> error("Invalid item id")
            }

        override fun getViewTypeCount(): Int = 2

        class ViewHolder(override val containerView: View) : LayoutContainer

        private companion object {
            const val TYPE_EMOTE = 0
            const val TYPE_USERNAME = 1
        }
    }
}
