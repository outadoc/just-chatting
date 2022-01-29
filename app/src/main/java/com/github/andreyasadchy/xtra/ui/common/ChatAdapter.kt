package com.github.andreyasadchy.xtra.ui.common

import android.graphics.Color
import android.graphics.Typeface
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.getSpans
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.GlideApp
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.chat.*
import com.github.andreyasadchy.xtra.ui.view.chat.animateGifs
import java.util.*
import kotlin.collections.set

class ChatAdapter(
        private val fragment: Fragment,
        private val emoteSize: Int,
        private val badgeSize: Int,
        private val randomColor: Boolean,
        private val boldNames: Boolean,
        private val badgeQuality: Int,
        private val enableZeroWidth: Boolean,
        private val firstChatMsg: String) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    var messages: MutableList<ChatMessage>? = null
        set(value) {
            val oldSize = field?.size ?: 0
            if (oldSize > 0) {
                notifyItemRangeRemoved(0, oldSize)
            }
            field = value
        }
    private val twitchColors = intArrayOf(-65536, -16776961, -16744448, -5103070, -32944, -6632142, -47872, -13726889, -2448096, -2987746, -10510688, -14774017, -38476, -7722014, -16711809)
    private val noColor = -10066329
    private val random = Random()
    private val userColors = HashMap<String, Int>()
    private val savedColors = HashMap<String, Int>()
    private var globalBadges: TwitchBadgesResponse? = null
    private var channelBadges: TwitchBadgesResponse? = null
    private val emotes = HashMap<String, Emote>()
    private var cheerEmotes: List<CheerEmote>? = null
    private var loggedInUser: String? = null
    private val scaledEmoteSize = (emoteSize * 0.78f).toInt()

    private var messageClickListener: ((CharSequence, CharSequence, String?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatMessage = messages?.get(position) ?: return
        val builder = SpannableStringBuilder()
        val images = ArrayList<Image>()
        var imageIndex = 0
        var badgesCount = 0
        if (chatMessage.isFirst) {
            builder.append("$firstChatMsg: \n")
            imageIndex += firstChatMsg.length + 3
        }
        chatMessage.badges?.forEach { badge ->
            var url: String?
            if (badge.id != "bits" && badge.id != "subscriber") {
                url = when (badgeQuality) {
                    3 -> (globalBadges?.getTwitchBadge(badge.id, badge.version)?.imageUrl4x)
                    2 -> (globalBadges?.getTwitchBadge(badge.id, badge.version)?.imageUrl2x)
                    else -> (globalBadges?.getTwitchBadge(badge.id, badge.version)?.imageUrl1x)
                }
            } else {
                url = when (badgeQuality) {
                    3 -> (channelBadges?.getTwitchBadge(badge.id, badge.version)?.imageUrl4x)
                    2 -> (channelBadges?.getTwitchBadge(badge.id, badge.version)?.imageUrl2x)
                    else -> (channelBadges?.getTwitchBadge(badge.id, badge.version)?.imageUrl1x)
                }
                if (url == null) {
                    url = when (badgeQuality) {
                        3 -> (globalBadges?.getTwitchBadge(badge.id, badge.version)?.imageUrl4x)
                        2 -> (globalBadges?.getTwitchBadge(badge.id, badge.version)?.imageUrl2x)
                        else -> (globalBadges?.getTwitchBadge(badge.id, badge.version)?.imageUrl1x)
                    }
                }
            }
            url?.let {
                builder.append("  ")
                images.add(Image(url, imageIndex++, imageIndex++, false))
                badgesCount++
            }
        }
        val userId = chatMessage.userId
        val userName = chatMessage.displayName
        val userNameLength = userName?.length ?: 0
        val userNameEndIndex = imageIndex + userNameLength
        val originalMessage: String
        val userNameWithPostfixLength: Int
        userName?.let { builder.append(it) }
        if (userName != null) {
            if (!chatMessage.isAction) {
                builder.append(": ")
                originalMessage = "$userName: ${chatMessage.message}"
                userNameWithPostfixLength = userNameLength + 2
            } else {
                builder.append(" ")
                originalMessage = "$userName ${chatMessage.message}"
                userNameWithPostfixLength = userNameLength + 1
            }
        } else {
            originalMessage = "${chatMessage.message}"
            userNameWithPostfixLength = 0
        }
        builder.append(chatMessage.message)
        val color = chatMessage.color.let { userColor ->
            if (userColor == null) {
                userColors[userName] ?: getRandomColor().also { if (userName != null) userColors[userName] = it }
            } else {
                savedColors[userColor] ?: Color.parseColor(userColor).also { savedColors[userColor] = it }
            }
        }
        if (userName != null) {
            builder.setSpan(ForegroundColorSpan(color), imageIndex, userNameEndIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(StyleSpan(if (boldNames) Typeface.BOLD else Typeface.NORMAL), imageIndex, userNameEndIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        try {
            chatMessage.emotes?.let { emotes ->
                val copy = emotes.map {
                    val realBegin = chatMessage.message?.offsetByCodePoints(0, it.begin) ?: 0
                    val realEnd = if (it.begin == realBegin) {
                        it.end
                    } else {
                        it.end + realBegin - it.begin
                    }
                    TwitchEmote(it.name, realBegin, realEnd)
                }
                imageIndex += userNameWithPostfixLength
                for (e in copy) {
                    val begin = imageIndex + e.begin
                    builder.replace(begin, imageIndex + e.end + 1, ".")
                    builder.setSpan(ForegroundColorSpan(Color.TRANSPARENT), begin, begin + 1, SPAN_EXCLUSIVE_EXCLUSIVE)
                    val length = e.end - e.begin
                    for (e1 in copy) {
                        if (e.begin < e1.begin) {
                            e1.begin -= length
                            e1.end -= length
                        }
                    }
                    e.end -= length
                }
                copy.forEach { images.add(Image(it.url, imageIndex + it.begin, imageIndex + it.end + 1, true, "image/gif")) }
            }
            val split = builder.split(" ")
            var builderIndex = 0
            var emotesFound = 0
            var wasMentioned = false
            for (value in split) {
                val length = value.length
                val endIndex = builderIndex + length
                var emote = emotes[value]
                val bits = value.takeLastWhile { it.isDigit() }
                if (bits != "") {
                    val name = value.substringBeforeLast(bits)
                    val cheerEmote = cheerEmotes?.findLast { it.name.equals(name, true) && it.minBits <= bits.toInt() }
                    if (cheerEmote != null) {
                        emote = cheerEmote
                        builder.insert(endIndex, bits)
                        if (emote.color != null) {
                            builder.setSpan(ForegroundColorSpan(Color.parseColor(emote.color)), endIndex, endIndex + bits.length, SPAN_INCLUSIVE_INCLUSIVE)
                        }
                    }
                }
                if (emote == null) {
                    if (!Patterns.WEB_URL.matcher(value).matches()) {
                        if (value.startsWith('@')) {
                            builder.setSpan(StyleSpan(if (boldNames) Typeface.BOLD else Typeface.NORMAL), builderIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        loggedInUser?.let {
                            if (!wasMentioned && value.contains(it, true) && chatMessage.userName != it) {
                                wasMentioned = true
                            }
                        }
                    } else {
                        val url = if (value.startsWith("http")) value else "https://$value"
                        builder.setSpan(URLSpan(url), builderIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    builderIndex += length + 1
                } else {
                    for (j in images.lastIndex - emotesFound downTo badgesCount) {
                        val e = images[j]
                        if (e.start > builderIndex) {
                            val remove = length - 1
                            e.start -= remove
                            e.end -= remove
                        }
                    }
                    builder.replace(builderIndex, endIndex, ".")
                    builder.setSpan(ForegroundColorSpan(Color.TRANSPARENT), builderIndex, builderIndex + 1, SPAN_EXCLUSIVE_EXCLUSIVE)
                    images.add(Image(emote.url, builderIndex, builderIndex + 1, true, emote.type, emote.zeroWidth))
                    emotesFound++
                    builderIndex += 2
                    if (emote is CheerEmote) {
                        builderIndex += bits.length
                    }
                }
            }
            if (chatMessage.isAction) {
                builder.setSpan(ForegroundColorSpan(color), if (userName != null) userNameEndIndex + 1 else 0, builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (chatMessage.isReward) {
                holder.textView.setBackgroundColor(R.attr.colorAccent)
            } else {
                if (wasMentioned && userId != null) {
                    builder.setSpan(ForegroundColorSpan(Color.WHITE), 0, builder.length, SPAN_INCLUSIVE_INCLUSIVE)
                    holder.textView.setBackgroundColor(Color.RED)
                } else {
                    if (chatMessage.isFirst) {
                        holder.textView.setBackgroundColor(Color.parseColor("#80404040"))
                    } else {
                        holder.textView.background = null
                    }
                }
            }
        } catch (e: Exception) {
//            Crashlytics.logException(e)
        }
        holder.bind(originalMessage, builder, userId)
        loadImages(holder, images, originalMessage, builder, userId)
    }

    override fun getItemCount(): Int = messages?.size ?: 0

    private fun loadImages(holder: ViewHolder, images: List<Image>, originalMessage: CharSequence, builder: SpannableStringBuilder, userId: String?) {
        images.forEach {
            if (it.type == "image/webp" && animateGifs) {
                loadWebp(holder, it, originalMessage, builder, userId)
            } else {
                if (it.type == "image/gif" && animateGifs) {
                    loadGif(holder, it, originalMessage, builder, userId)
                } else {
                    loadDrawable(holder, it, originalMessage, builder, userId)
                }
            }
        }
    }

    private fun loadWebp(holder: ViewHolder, image: Image, originalMessage: CharSequence, builder: SpannableStringBuilder, userId: String?) {
        GlideApp.with(fragment)
            .asWebp()
            .load(image.url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(object : CustomTarget<WebpDrawable>() {
                override fun onResourceReady(resource: WebpDrawable, transition: Transition<in WebpDrawable>?) {
                    resource.apply {
                        val size = calculateEmoteSize(this)
                        if (image.zerowidth && enableZeroWidth) {
                            setBounds(-90, 0, size.first - 90, size.second)
                        } else {
                            setBounds(0, 0, size.first, size.second)
                        }
                        loopCount = WebpDrawable.LOOP_FOREVER
                        callback = object : Drawable.Callback {
                            override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                                holder.textView.removeCallbacks(what)
                            }

                            override fun invalidateDrawable(who: Drawable) {
                                holder.textView.invalidate()
                            }

                            override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
                                holder.textView.postDelayed(what, `when`)
                            }
                        }
                        start()
                    }
                    try {
                        builder.setSpan(ImageSpan(resource), image.start, image.end, SPAN_EXCLUSIVE_EXCLUSIVE)
                    } catch (e: IndexOutOfBoundsException) {
                    }
                    holder.bind(originalMessage, builder, userId)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    loadDrawable(holder, image, originalMessage, builder, userId)
                }
            })
    }

    private fun loadGif(holder: ViewHolder, image: Image, originalMessage: CharSequence, builder: SpannableStringBuilder, userId: String?) {
        GlideApp.with(fragment)
            .asGif()
            .load(image.url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(object : CustomTarget<GifDrawable>() {
                override fun onResourceReady(resource: GifDrawable, transition: Transition<in GifDrawable>?) {
                    resource.apply {
                        val size = calculateEmoteSize(this)
                        if (image.zerowidth && enableZeroWidth) {
                            setBounds(-90, 0, size.first - 90, size.second)
                        } else {
                            setBounds(0, 0, size.first, size.second)
                        }
                        setLoopCount(GifDrawable.LOOP_FOREVER)
                        callback = object : Drawable.Callback {
                            override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                                holder.textView.removeCallbacks(what)
                            }

                            override fun invalidateDrawable(who: Drawable) {
                                holder.textView.invalidate()
                            }

                            override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
                                holder.textView.postDelayed(what, `when`)
                            }
                        }
                        start()
                    }
                    try {
                        builder.setSpan(ImageSpan(resource), image.start, image.end, SPAN_EXCLUSIVE_EXCLUSIVE)
                    } catch (e: IndexOutOfBoundsException) {
                    }
                    holder.bind(originalMessage, builder, userId)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    loadDrawable(holder, image, originalMessage, builder, userId)
                }
            })
    }

    private fun loadDrawable(holder: ViewHolder, image: Image, originalMessage: CharSequence, builder: SpannableStringBuilder, userId: String?) {
        GlideApp.with(fragment)
            .load(image.url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    val width: Int
                    val height: Int
                    if (image.isEmote) {
                        val size = calculateEmoteSize(resource)
                        width = size.first
                        height = size.second
                    } else {
                        width = badgeSize
                        height = badgeSize
                    }
                    if (image.zerowidth && enableZeroWidth) {
                        resource.setBounds(-90, 0, width - 90, height)
                    } else {
                        resource.setBounds(0, 0, width, height)
                    }
                    try {
                        builder.setSpan(ImageSpan(resource), image.start, image.end, SPAN_EXCLUSIVE_EXCLUSIVE)
                    } catch (e: IndexOutOfBoundsException) {
                    }
                    holder.bind(originalMessage, builder, userId)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    fun addGlobalBadges(list: TwitchBadgesResponse) {
        globalBadges = list
    }

    fun addChannelBadges(list: TwitchBadgesResponse) {
        channelBadges = list
    }

    fun addCheerEmotes(list: List<CheerEmote>) {
        cheerEmotes = list
    }

    fun addEmotes(list: List<Emote>) {
        emotes.putAll(list.associateBy { it.name })
    }

    fun setUsername(username: String) {
        this.loggedInUser = username
    }

    fun setOnClickListener(listener: (CharSequence, CharSequence, String?) -> Unit) {
        messageClickListener = listener
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        (holder.textView.text as? Spannable)?.getSpans<ImageSpan>()?.forEach {
            (it.drawable as? GifDrawable)?.start()
            (it.drawable as? WebpDrawable)?.start()
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        (holder.textView.text as? Spannable)?.getSpans<ImageSpan>()?.forEach {
            (it.drawable as? GifDrawable)?.stop()
            (it.drawable as? WebpDrawable)?.stop()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        val childCount = recyclerView.childCount
        for (i in 0 until childCount) {
            ((recyclerView.getChildAt(i) as TextView).text as? Spannable)?.getSpans<ImageSpan>()?.forEach {
                (it.drawable as? GifDrawable)?.stop()
                (it.drawable as? WebpDrawable)?.stop()
            }
        }
        super.onDetachedFromRecyclerView(recyclerView)
    }

    private fun getRandomColor(): Int =
        if (randomColor) {
            twitchColors[random.nextInt(twitchColors.size)]
        } else {
            noColor
        }

    private fun calculateEmoteSize(resource: Drawable): Pair<Int, Int> {
        val widthRatio = resource.intrinsicWidth.toFloat() / resource.intrinsicHeight.toFloat()
        val width: Int
        val height: Int
        when {
            widthRatio == 1f -> {
                width = emoteSize
                height = emoteSize
            }
            widthRatio <= 1.2f -> {
                width = (emoteSize * widthRatio).toInt()
                height = emoteSize
            }
            else -> {
                width = (scaledEmoteSize * widthRatio).toInt()
                height = scaledEmoteSize
            }
        }
        return width to height
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textView = itemView as TextView

        fun bind(originalMessage: CharSequence, formattedMessage: SpannableStringBuilder, userId: String?) {
            textView.apply {
                text = formattedMessage
                movementMethod = LinkMovementMethod.getInstance()
                setOnClickListener { messageClickListener?.invoke(originalMessage, formattedMessage, userId) }
            }
        }
    }
}
