package fr.outadoc.justchatting.ui.view.chat

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.ViewChatInputBinding
import fr.outadoc.justchatting.model.chat.Chatter
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.ui.chat.MessagePostConstraint
import fr.outadoc.justchatting.util.hideKeyboard
import fr.outadoc.justchatting.util.showKeyboard
import kotlinx.datetime.Clock

class ChatInputView : LinearLayout {

    fun interface OnMessageSendListener {
        fun send(message: CharSequence)
    }

    private val _emotePickerSelectedTab = MutableLiveData<Int>()
    val emotePickerSelectedTab: LiveData<Int> = _emotePickerSelectedTab

    private val autoCompleteAdapter = AutoCompleteAdapter(context)

    private var messageCallback: OnMessageSendListener? = null
    private var progressAnimator: ValueAnimator? = null

    private lateinit var viewHolder: ViewChatInputBinding

    var animateEmotes: Boolean
        get() = autoCompleteAdapter.animateEmotes
        set(value) {
            autoCompleteAdapter.animateEmotes = value
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
        viewHolder = ViewChatInputBinding.inflate(LayoutInflater.from(context), this)
        orientation = VERTICAL

        viewHolder.apply {
            editText.setAdapter(autoCompleteAdapter)
            editText.setTokenizer(AutoCompleteSpaceTokenizer())

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
                if (emotePicker.isGone) {
                    editText.hideKeyboard()
                    emotePicker.isVisible = true
                } else {
                    editText.showKeyboard()
                    emotePicker.isVisible = false
                }
            }

            send.setOnClickListener { sendMessage() }
            messageView.isVisible = true

            emotePickerTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {
                    val position = tab?.position ?: return
                    _emotePickerSelectedTab.postValue(position)
                }
            })
        }

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
            val navBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            viewHolder.apply {
                messageView.setPadding(
                    messageView.paddingLeft,
                    messageView.paddingTop,
                    messageView.paddingRight,
                    if (imeInsets.bottom > 0) imeInsets.bottom else navBarInsets.bottom
                )

                if (imeInsets.bottom > 0) {
                    // Hide emote picker when keyboard is opened
                    hideEmotesMenu()

                    // Set emote picker height to keyboard height
                    emotePicker.updateLayoutParams {
                        height = imeInsets.bottom
                    }
                }
            }

            windowInsets
        }
    }

    var emotePickerAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>?
        get() = viewHolder.emotePickerPager.adapter
        set(value) {
            viewHolder.emotePickerPager.adapter = value

            TabLayoutMediator(
                viewHolder.emotePickerTabLayout,
                viewHolder.emotePickerPager
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> context.getString(R.string.emote_tab_recent)
                    1 -> context.getString(R.string.emote_tab_twitch)
                    else -> context.getString(R.string.emote_tab_others)
                }
            }.attach()
        }

    fun setAutocompleteItems(emotes: Collection<Emote>, chatters: Collection<Chatter>) {
        val allItems =
            chatters.map { AutoCompleteItem.UserItem(it) } +
                emotes.distinctBy { emote -> emote.name }
                    .map { AutoCompleteItem.EmoteItem(it) }

        if (allItems.count() != autoCompleteAdapter.count) {
            autoCompleteAdapter.clear()
            autoCompleteAdapter.addAll(allItems)
        }
    }

    fun setOnMessageSendListener(callback: OnMessageSendListener) {
        messageCallback = callback
    }

    fun setMessagePostConstraint(constraint: MessagePostConstraint?) {
        // No constraint, no need for an indicator
        if (constraint == null) {
            viewHolder.progressCannotSendUntil.isInvisible = true
            return
        }

        // If the constraint is in the past, just ignore it
        val now = Clock.System.now()
        val canPostAt = constraint.lastMessageSentAt + constraint.slowModeDuration

        if (canPostAt < now) {
            viewHolder.progressCannotSendUntil.isInvisible = true
            return
        }

        if (progressAnimator != null) {
            progressAnimator?.cancel()
            progressAnimator = null
        }

        progressAnimator = ValueAnimator.ofInt(0, Int.MAX_VALUE)
            .apply {
                interpolator = null
                duration = constraint.slowModeDuration.inWholeMilliseconds

                addUpdateListener { animation ->
                    with(viewHolder.progressCannotSendUntil) {
                        max = Int.MAX_VALUE
                        progress = animation.animatedValue as Int
                        isInvisible = progress == 0
                    }
                }

                reverse()

                currentPlayTime = (now - constraint.lastMessageSentAt).inWholeMilliseconds
            }
    }

    fun hideEmotesMenu(): Boolean {
        return if (viewHolder.emotePicker.isVisible) {
            viewHolder.emotePicker.isVisible = false
            true
        } else {
            false
        }
    }

    fun appendEmote(emote: Emote) {
        viewHolder.editText.text.append(emote.name).append(' ')
    }

    fun reply(userName: CharSequence) {
        val text = "@$userName "
        viewHolder.editText.apply {
            setText(text)
            setSelection(text.length)
            showKeyboard()
        }
    }

    fun setMessage(text: CharSequence) {
        viewHolder.editText.setText(text)
    }

    private fun sendMessage(): Boolean {
        viewHolder.editText.hideKeyboard()
        viewHolder.editText.clearFocus()

        hideEmotesMenu()

        return messageCallback?.let { listener ->
            val text = viewHolder.editText.text.trim()
            viewHolder.editText.text.clear()
            if (text.isNotEmpty()) {
                listener.send(text)
                true
            } else {
                false
            }
        } == true
    }
}
