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
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.ChatMessage
import com.github.andreyasadchy.xtra.model.chat.CheerEmote
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.model.chat.Image
import com.github.andreyasadchy.xtra.model.chat.LiveChatMessage
import com.github.andreyasadchy.xtra.model.chat.PubSubPointReward
import com.github.andreyasadchy.xtra.model.chat.TwitchBadge
import com.github.andreyasadchy.xtra.model.chat.TwitchEmote
import com.github.andreyasadchy.xtra.ui.view.chat.VerticalImageSpan
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.formatTimestamp
import com.github.andreyasadchy.xtra.util.isDarkMode
import kotlin.collections.set

class ChatAdapter(
    private val context: Context,
    private val enableTimestamps: Boolean,
    private val animateEmotes: Boolean
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private companion object {
        const val ITEM_TYPE_MESSAGE = 0
        const val ITEM_TYPE_NOTICE = 1
    }

    var messages: MutableList<ChatMessage>? = null
        set(value) {
            val oldSize = field?.size ?: 0
            if (oldSize > 0) {
                notifyItemRangeRemoved(0, oldSize)
            }
            field = value
        }

    private val screenDensity get() = context.resources.displayMetrics.density

    private val badgeSize = context.resources.getDimensionPixelSize(R.dimen.chat_badgeSize)
    private val emoteSize = context.resources.getDimensionPixelSize(R.dimen.chat_emoteSize)
    private val scaledEmoteSize = (emoteSize * 0.78f).toInt()

    private val randomChatColors = context.resources.getIntArray(R.array.randomChatColors)

    @ColorInt
    private val defaultChatColor = ContextCompat.getColor(context, R.color.chatUserColorFallback)

    @get:ColorInt
    private val backgroundColor: Int by lazy {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorSurface, typedValue, true)
        typedValue.data
    }

    private val redeemedChatMsg = context.getString(R.string.redeemed)
    private val redeemedNoMsg = context.getString(R.string.user_redeemed)

    private val userColors = HashMap<String, Int>()
    private val savedColors = HashMap<String, Int>()
    private var globalBadges: List<TwitchBadge>? = null
    private var channelBadges: List<TwitchBadge>? = null
    private val emotes = HashMap<String, Emote>()
    private var cheerEmotes: List<CheerEmote>? = null
    private var loggedInUser: String? = null

    fun interface OnMessageClickListener {
        fun onMessageClick(
            originalMessage: CharSequence,
            formattedMessage: CharSequence,
            userId: String?,
            fullMsg: String?
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
        val chatMessage = messages?.get(position) as? LiveChatMessage
            ?: return ITEM_TYPE_MESSAGE

        val isFirstMessage = chatMessage.isFirst
        val isRewarded = chatMessage.rewardId != null
        val isNotice = chatMessage.systemMsg != null || chatMessage.msgId != null
        val isAction = chatMessage.isAction

        return when {
            isFirstMessage || isRewarded || isNotice || isAction -> ITEM_TYPE_NOTICE
            else -> ITEM_TYPE_MESSAGE
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMessage = messages?.get(position) ?: return
        bindNoticeMessage(holder, chatMessage)
        bindMessage(holder, chatMessage)
    }

    private fun bindNoticeMessage(holder: ViewHolder, chatMessage: ChatMessage) {
        val liveMessage: LiveChatMessage? = chatMessage as? LiveChatMessage
        val pointReward: PubSubPointReward? = chatMessage as? PubSubPointReward
            ?: liveMessage?.pointReward

        holder.noticeTitle?.apply {
            text = null

            val noticeMessage = liveMessage?.msgId?.let { messageId ->
                TwitchApiHelper.getMessageIdString(context, messageId) ?: liveMessage.msgId
            }

            when {
                liveMessage?.systemMsg != null -> {
                    text = liveMessage.systemMsg
                }
                noticeMessage != null -> {
                    text = noticeMessage
                }
                liveMessage?.isFirst == true -> {
                    setText(R.string.chat_first)
                }
                liveMessage?.rewardId != null && pointReward == null -> {
                    setText(R.string.chat_reward)
                }
            }

            isVisible = text.isNotEmpty()
        }
    }

    private fun bindMessage(holder: ViewHolder, chatMessage: ChatMessage) {
        val liveMessage: LiveChatMessage? = chatMessage as? LiveChatMessage
        val pointReward: PubSubPointReward? = chatMessage as? PubSubPointReward
            ?: liveMessage?.pointReward

        val images: MutableList<Image> = mutableListOf()
        var imageIndex = 0
        var badgesCount = 0

        val builder = SpannableStringBuilder()

        if (!pointReward?.message.isNullOrBlank()) {
            val string = redeemedChatMsg.format(pointReward?.rewardTitle)
            builder.append("$string ")
            imageIndex += string.length + 1

            pointReward?.getUrl(screenDensity)?.let { url ->
                builder.append("  ")
                images.add(Image(url, imageIndex++, imageIndex++, false))
                badgesCount++
            }

            builder.append("${pointReward?.rewardCost}\n")
            imageIndex += (pointReward?.rewardCost?.toString()?.length ?: 0) + 1
        }

        val timestamp: String? =
            (liveMessage?.timestamp ?: pointReward?.timestamp)?.formatTimestamp(context)

        if (enableTimestamps && timestamp != null) {
            builder.append("$timestamp ")
            builder.setSpan(
                ForegroundColorSpan(defaultChatColor),
                imageIndex,
                imageIndex + timestamp.length,
                SPAN_INCLUSIVE_INCLUSIVE
            )
            imageIndex += timestamp.length + 1
        }

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

        val fullMsg = chatMessage.fullMsg
        val userId = chatMessage.userId
        val userName = chatMessage.userName
        val userNameLength = userName?.length ?: 0
        val userNameEndIndex = imageIndex + userNameLength
        val originalMessage: String
        val userNameWithPostfixLength: Int

        if (chatMessage !is PubSubPointReward && !userName.isNullOrBlank()) {
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
            if (chatMessage is PubSubPointReward && pointReward?.message.isNullOrBlank()) {
                val string = redeemedNoMsg.format(userName, pointReward?.rewardTitle)
                builder.append("$string ")
                imageIndex += string.length + 1

                pointReward?.getUrl(screenDensity)?.let { url ->
                    builder.append("  ")
                    images.add(
                        Image(
                            url = url,
                            start = imageIndex++,
                            end = imageIndex++,
                            isEmote = false
                        )
                    )
                    badgesCount++
                }

                builder.append("${pointReward?.rewardCost}")
                imageIndex += pointReward?.rewardCost?.toString()?.length ?: 0
                originalMessage = "$userName: ${chatMessage.message}"
                userNameWithPostfixLength =
                    string.length + (pointReward?.rewardCost?.toString()?.length ?: 0) + 3

                builder.setSpan(
                    ForegroundColorSpan(defaultChatColor),
                    userNameWithPostfixLength - userNameWithPostfixLength,
                    userNameWithPostfixLength,
                    SPAN_INCLUSIVE_INCLUSIVE
                )
            } else {
                originalMessage = "${chatMessage.message}"
                userNameWithPostfixLength = 0
            }
        }

        builder.append(chatMessage.message)

        val color = if (chatMessage is PubSubPointReward) null
        else getColorForUser(
            userName = userName,
            messageColor = chatMessage.color
        )

        if (color != null && userName != null) {
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

        holder.bind(
            originalMessage = originalMessage,
            formattedMessage = builder,
            userId = userId,
            fullMsg = fullMsg
        )

        loadImages(
            holder = holder,
            images = images,
            originalMessage = originalMessage,
            builder = builder,
            userId = userId,
            fullMsg = fullMsg
        )
    }

    private fun loadImages(
        holder: ViewHolder,
        images: List<Image>,
        originalMessage: CharSequence,
        builder: SpannableStringBuilder,
        userId: String?,
        fullMsg: String?
    ) {
        images.forEach { image ->
            loadCoil(
                holder = holder,
                image = image,
                originalMessage = originalMessage,
                builder = builder,
                userId = userId,
                fullMsg = fullMsg
            )
        }
    }

    private fun loadCoil(
        holder: ViewHolder,
        image: Image,
        originalMessage: CharSequence,
        builder: SpannableStringBuilder,
        userId: String?,
        fullMsg: String?
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

                    holder.bind(originalMessage, builder, userId, fullMsg)
                },
            )
            .build()

        context.imageLoader.enqueue(request)
    }

    override fun getItemCount(): Int = messages?.size ?: 0

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

        (holder.textView.text as? Spannable)
            ?.getSpans<ImageSpan>()
            ?.map { it.drawable }
            ?.filterIsInstance<Animatable>()
            ?.forEach { image -> image.start() }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (!animateEmotes) return

        (holder.textView.text as? Spannable)
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
        val userColor = userColors[userName]
        val savedColor = savedColors[messageColor]
        return when {
            userColor != null -> userColor
            savedColor != null -> savedColor
            messageColor == null -> getAndSaveRandomUserColor(userName)
            else -> {
                val color = Color.parseColor(messageColor)
                if (isContrastRatioAccessible(backgroundColor, color)) {
                    saveUserColor(messageColor)
                } else {
                    getAndSaveRandomUserColor(userName)
                }
            }
        }
    }

    @ColorInt
    private fun getAndSaveRandomUserColor(userName: String?): Int {
        return getRandomColor().also {
            if (userName != null) {
                userColors[userName] = it
            }
        }
    }

    @ColorInt
    private fun saveUserColor(userColor: String): Int {
        return Color.parseColor(userColor).also { color ->
            savedColors[userColor] = color
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

        val textView: TextView = itemView.findViewById(R.id.textView_chatMessage)
        val noticeTitle: TextView? = itemView.findViewById(R.id.textView_chatNoticeTitle)

        fun bind(
            originalMessage: CharSequence,
            formattedMessage: SpannableStringBuilder,
            userId: String?,
            fullMsg: String?
        ) {
            textView.apply {
                text = formattedMessage
                movementMethod = LinkMovementMethod.getInstance()
                setOnClickListener {
                    messageClickListener?.onMessageClick(
                        originalMessage = originalMessage,
                        formattedMessage = formattedMessage,
                        userId = userId,
                        fullMsg = fullMsg
                    )
                }
            }
        }
    }
}
