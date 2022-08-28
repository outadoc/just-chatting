package com.github.andreyasadchy.xtra.ui.common

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.util.Patterns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.getSpans
import androidx.core.text.toSpannable
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.Image
import com.github.andreyasadchy.xtra.model.chat.TwitchBadge
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.ui.view.chat.VerticalImageSpan
import com.github.andreyasadchy.xtra.ui.view.chat.model.ChatEntry
import com.github.andreyasadchy.xtra.util.formatTimestamp
import com.github.andreyasadchy.xtra.util.isDarkMode
import kotlin.collections.set

class ChatAdapter(
    private val context: Context,
) : ListAdapter<ChatEntry, ChatAdapter.ViewHolder>(ChatEntryDiffUtil) {

    private companion object {
        const val ITEM_TYPE_MESSAGE = 0
        const val ITEM_TYPE_NOTICE = 1
    }

    var showTimestamps: Boolean = false
    var animateEmotes: Boolean = true

    private val screenDensity get() = context.resources.displayMetrics.density

    private val badgeSize = context.resources.getDimensionPixelSize(R.dimen.chat_badgeSize)
    private val emoteSize = context.resources.getDimensionPixelSize(R.dimen.chat_emoteSize)
    private val scaledEmoteSize = (emoteSize * 0.78f).toInt()

    private val randomChatColors = context.resources.getIntArray(R.array.randomChatColors)

    @get:ColorInt
    private val backgroundColor: Int by lazy {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorSurface, typedValue, true)
        typedValue.data
    }

    private val userColors = HashMap<String, Int>()
    private var globalBadges: List<TwitchBadge>? = null
    private var channelBadges: List<TwitchBadge>? = null
    private val emotes = HashMap<String, Emote>()
    private var cheerEmotes: List<CheerEmote>? = null
    private var loggedInUser: String? = null

    fun interface OnMessageClickListener {
        fun onMessageClick(
            originalMessage: CharSequence,
            formattedMessage: CharSequence,
            userId: String?
        )
    }

    private var messageClickListener: OnMessageClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewId = when (viewType) {
            ITEM_TYPE_NOTICE -> R.layout.chat_list_notice_item
            else -> R.layout.chat_list_item
        }

        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(viewId, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChatEntry.Simple -> ITEM_TYPE_MESSAGE
            is ChatEntry.Highlighted -> ITEM_TYPE_NOTICE
            null -> error("Invalid item type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMessage = getItem(position) ?: return

        holder.bindTimestamp(
            timestamp = chatMessage.timestamp
                ?.formatTimestamp(context)
                ?.takeIf { showTimestamps }
        )

        when (chatMessage) {
            is ChatEntry.Highlighted -> {
                /*
                if (headerImage != null) {
                    pointReward?.getUrl(screenDensity)?.let { url ->
                        builder.append("  ")
                        images.add(Image(url, imageIndex++, imageIndex++, false))
                        badgesCount++
                    }

                    builder.append("${pointReward?.rewardCost}\n")
                    imageIndex += (pointReward?.rewardCost?.toString()?.length ?: 0) + 1
                }
                */

                holder.noticeTitle?.apply {
                    text = chatMessage.header
                    setCompoundDrawablesRelative(
                        chatMessage.headerIconResId?.let { resId ->
                            AppCompatResources.getDrawable(context, resId)?.apply {
                                setBounds(0, 0, minimumWidth, minimumHeight)
                            }
                        },
                        null,
                        null,
                        null
                    )
                    isVisible = text.isNotEmpty()
                }
            }
            is ChatEntry.Simple -> {}
        }

        when (val data = chatMessage.data) {
            is ChatEntry.Data.Rich -> {
                bindMessage(holder, data)
            }
            is ChatEntry.Data.Plain -> {
                data.message?.let {
                    holder.bind(
                        originalMessage = data.message,
                        formattedMessage = data.message.toSpannable(),
                        userId = null,
                        inReplyTo = null
                    )
                }
            }
            null -> holder.clearMessage()
        }
    }

    private fun bindMessage(holder: ViewHolder, chatMessage: ChatEntry.Data.Rich) {
        val images: MutableList<Image> = mutableListOf()
        var imageIndex = 0
        var badgesCount = 0

        val builder = SpannableStringBuilder()

        chatMessage.badges?.forEach { chatBadge ->
            val badge: TwitchBadge? =
                channelBadges?.find { it.id == chatBadge.id && it.version == chatBadge.version }
                    ?: globalBadges?.find { it.id == chatBadge.id && it.version == chatBadge.version }

            badge?.getUrl(screenDensity)?.let { badgeUrl ->
                builder.append("  ")
                images.add(
                    Image(
                        url = badgeUrl,
                        start = imageIndex++,
                        end = imageIndex++,
                        isEmote = false
                    )
                )
                badgesCount++
            }
        }

        val userId = chatMessage.userId
        val userName = chatMessage.userName
        val userNameLength = userName?.length ?: 0
        val userNameEndIndex = imageIndex + userNameLength
        val originalMessage: String
        val userNameWithPostfixLength: Int

        if (!userName.isNullOrEmpty()) {
            builder.append(userName)
            if (chatMessage.isAction) {
                builder.append(" ")
                originalMessage = "$userName ${chatMessage.message}"
                userNameWithPostfixLength = userNameLength + 1
            } else {
                builder.append(": ")
                originalMessage = "$userName: ${chatMessage.message}"
                userNameWithPostfixLength = userNameLength + 2
            }
        } else {
            originalMessage = ""
            userNameWithPostfixLength = 0
        }

        chatMessage.message?.let { message ->
            builder.append(message)
        }

        val color = getColorForUser(
            userName = userName,
            messageColor = chatMessage.color
        )

        if (userName != null) {
            builder.setSpan(
                ForegroundColorSpan(color),
                imageIndex,
                userNameEndIndex,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                StyleSpan(Typeface.BOLD),
                imageIndex,
                userNameEndIndex,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        try {
            chatMessage.emotes?.let { emotes ->
                val emotesCopy = emotes.map { emote ->
                    val realBegin = chatMessage.message?.offsetByCodePoints(0, emote.begin) ?: 0
                    val realEnd = if (emote.begin == realBegin) {
                        emote.end
                    } else {
                        emote.end + realBegin - emote.begin
                    }

                    TwitchEmote(
                        name = emote.name,
                        id = emote.id,
                        begin = realBegin,
                        end = realEnd
                    )
                }

                imageIndex += userNameWithPostfixLength

                emotesCopy.forEach { emote ->
                    val begin = imageIndex + emote.begin

                    builder.replace(begin, imageIndex + emote.end + 1, ".")
                    builder.setSpan(
                        ForegroundColorSpan(Color.TRANSPARENT),
                        begin,
                        begin + 1,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    val length = emote.end - emote.begin
                    emotesCopy.forEach { other ->
                        if (emote.begin < other.begin) {
                            other.begin -= length
                            other.end -= length
                        }
                    }

                    emote.end -= length
                }

                emotesCopy.forEach { emote ->
                    images.add(
                        Image(
                            url = emote.getUrl(
                                animate = animateEmotes,
                                screenDensity = screenDensity,
                                isDarkTheme = context.isDarkMode
                            ),
                            start = imageIndex + emote.begin,
                            end = imageIndex + emote.end + 1,
                            isEmote = true
                        )
                    )
                }
            }

            val words = builder.split(" ")
            var builderIndex = 0
            var emotesFound = 0
            var wasMentioned = false

            words.forEach { word ->
                val length = word.length
                val endIndex = builderIndex + length
                var emote = emotes[word]
                val bitsCount = word.takeLastWhile { it.isDigit() }
                val bitsName = word.substringBeforeLast(bitsCount)

                if (bitsCount.isNotEmpty()) {
                    val cheerEmote = cheerEmotes?.findLast { cheerEmote ->
                        cheerEmote.name == bitsName && cheerEmote.minBits <= bitsCount.toInt()
                    }

                    if (cheerEmote != null) {
                        emote = cheerEmote
                        if (emote.color != null) {
                            builder.setSpan(
                                ForegroundColorSpan(Color.parseColor(emote.color)),
                                builderIndex + bitsName.length,
                                endIndex,
                                SPAN_INCLUSIVE_INCLUSIVE
                            )
                        }
                    }
                }

                if (emote == null) {
                    if (!Patterns.WEB_URL.matcher(word).matches()) {
                        if (word.startsWith('@')) {
                            builder.setSpan(
                                StyleSpan(Typeface.BOLD),
                                builderIndex,
                                endIndex,
                                SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        loggedInUser?.let { loggedInUserName ->
                            if (!wasMentioned &&
                                word.contains(loggedInUserName, ignoreCase = true) &&
                                chatMessage.userLogin != loggedInUserName
                            ) {
                                wasMentioned = true
                            }
                        }
                    } else {
                        val url = if (word.startsWith("http")) word else "https://$word"
                        builder.setSpan(
                            URLSpan(url),
                            builderIndex,
                            endIndex,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    builderIndex += length + 1
                } else {
                    for (j in images.lastIndex - emotesFound downTo badgesCount) {
                        val e = images[j]
                        if (e.start > builderIndex) {
                            val remove = if (emote is CheerEmote) {
                                length - 1 - bitsCount.length
                            } else {
                                length - 1
                            }
                            e.start -= remove
                            e.end -= remove
                        }
                    }

                    if (emote is CheerEmote) {
                        builder.replace(builderIndex, builderIndex + bitsName.length, ".")
                    } else {
                        builder.replace(builderIndex, endIndex, ".")
                    }

                    builder.setSpan(
                        ForegroundColorSpan(Color.TRANSPARENT),
                        builderIndex,
                        builderIndex + 1,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    images.add(
                        Image(
                            url = emote.getUrl(
                                animate = animateEmotes,
                                screenDensity = screenDensity,
                                isDarkTheme = context.isDarkMode
                            ),
                            start = builderIndex,
                            end = builderIndex + 1,
                            isEmote = true
                        )
                    )

                    emotesFound++
                    builderIndex += 2

                    if (emote is CheerEmote) {
                        builderIndex += bitsCount.length
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val inReplyTo = chatMessage.inReplyTo?.let { inReplyTo ->
            buildSpannedString {
                bold { append("@${inReplyTo.userName}") }
                append(": ${inReplyTo.message}")
            }
        }

        holder.bind(
            originalMessage = originalMessage,
            formattedMessage = builder,
            userId = userId,
            inReplyTo = inReplyTo
        )

        loadImages(
            holder = holder,
            images = images,
            originalMessage = originalMessage,
            builder = builder,
            userId = userId
        )
    }

    private fun loadImages(
        holder: ViewHolder,
        images: List<Image>,
        originalMessage: CharSequence,
        builder: SpannableStringBuilder,
        userId: String?
    ) {
        images.forEach { image ->
            loadCoil(
                holder = holder,
                image = image,
                originalMessage = originalMessage,
                builder = builder,
                userId = userId
            )
        }
    }

    private fun loadCoil(
        holder: ViewHolder,
        image: Image,
        originalMessage: CharSequence,
        builder: SpannableStringBuilder,
        userId: String?
    ) {
        val request = ImageRequest.Builder(context)
            .data(image.url)
            .target(
                onSuccess = { result ->
                    val (width, height) =
                        if (image.isEmote) calculateEmoteSize(result)
                        else badgeSize to badgeSize

                    if (image.isZeroWidth) {
                        result.setBounds(-90, 0, width - 90, height)
                    } else {
                        result.setBounds(0, 0, width, height)
                    }

                    try {
                        builder.setSpan(
                            VerticalImageSpan(result),
                            image.start,
                            image.end,
                            SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        if (animateEmotes) {
                            (result as? Animatable)?.start()
                        }
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }

                    holder.bind(
                        originalMessage = originalMessage,
                        formattedMessage = builder,
                        userId = userId,
                        inReplyTo = null
                    )
                }
            )
            .build()

        context.imageLoader.enqueue(request)
    }

    fun addGlobalBadges(list: List<TwitchBadge>) {
        globalBadges = list
    }

    fun addChannelBadges(list: List<TwitchBadge>) {
        channelBadges = list
    }

    fun addCheerEmotes(list: List<CheerEmote>) {
        cheerEmotes = list
    }

    fun addEmotes(list: Collection<Emote>) {
        emotes.putAll(list.associateBy { it.name })
    }

    fun setUsername(username: String) {
        this.loggedInUser = username
    }

    fun setOnClickListener(listener: OnMessageClickListener?) {
        messageClickListener = listener
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (!animateEmotes) return

        (holder.message.text as? Spannable)
            ?.getSpans<ImageSpan>()
            ?.map { it.drawable }
            ?.filterIsInstance<Animatable>()
            ?.forEach { image -> image.start() }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (!animateEmotes) return

        (holder.message.text as? Spannable)
            ?.getSpans<ImageSpan>()
            ?.map { it.drawable }
            ?.filterIsInstance<Animatable>()
            ?.forEach { image -> image.stop() }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        if (!animateEmotes) return

        recyclerView.children
            .filterIsInstance<TextView>()
            .forEach { message ->
                (message.text as? Spannable)
                    ?.getSpans<ImageSpan>()
                    ?.map { it.drawable }
                    ?.filterIsInstance<Animatable>()
                    ?.forEach { image -> image.stop() }
            }
    }

    private fun getColorForUser(userName: String?, messageColor: String?): Int {
        val savedColor = userColors[userName]
        val accessibleColor = messageColor?.let { color ->
            ensureColorIsAccessible(
                foreground = Color.parseColor(color),
                background = backgroundColor
            )
        }

        return savedColor ?: accessibleColor ?: getAndSaveRandomUserColor(userName)
    }

    @ColorInt
    private fun getAndSaveRandomUserColor(userName: String?): Int {
        return getRandomColor().also {
            if (userName != null) {
                userColors[userName] = it
            }
        }
    }

    private fun getRandomColor(): Int = randomChatColors.random()

    private fun calculateEmoteSize(resource: Drawable): Pair<Int, Int> {
        val widthRatio = resource.intrinsicWidth.toFloat() / resource.intrinsicHeight.toFloat()

        return when {
            widthRatio == 1f -> emoteSize to emoteSize
            widthRatio <= 1.2f -> (emoteSize * widthRatio).toInt() to emoteSize
            else -> (scaledEmoteSize * widthRatio).toInt() to scaledEmoteSize
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val message: TextView = itemView.findViewById(R.id.textView_chatMessage)
        val noticeTitle: TextView? = itemView.findViewById(R.id.textView_chatNoticeTitle)
        private val inReplyTo: TextView = itemView.findViewById(R.id.textView_inReplyTo)
        private val messageContainer: ViewGroup? = itemView.findViewById(R.id.messageContainer)
        private val timestamp: TextView = itemView.findViewById(R.id.textView_timestamp)

        fun clearMessage() {
            messageContainer?.isVisible = false
            inReplyTo.isVisible = false
        }

        fun bind(
            originalMessage: CharSequence,
            formattedMessage: Spannable,
            userId: String?,
            inReplyTo: CharSequence?
        ) {
            messageContainer?.isVisible = true

            message.apply {
                text = formattedMessage
                movementMethod = LinkMovementMethod.getInstance()
                setOnClickListener {
                    messageClickListener?.onMessageClick(
                        originalMessage = originalMessage,
                        formattedMessage = formattedMessage,
                        userId = userId
                    )
                }
            }

            this.inReplyTo.isVisible = inReplyTo != null
            this.inReplyTo.text = inReplyTo
        }

        fun bindTimestamp(timestamp: String?) {
            if (timestamp.isNullOrEmpty()) {
                this.timestamp.isVisible = false
            } else {
                this.timestamp.isVisible = true
                this.timestamp.text = timestamp
            }
        }
    }
}
