package com.github.andreyasadchy.xtra.ui.channel

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.chat.ChatFragment
import com.github.andreyasadchy.xtra.ui.common.pagers.ItemAwareFragmentPagerAdapter
import com.github.andreyasadchy.xtra.util.C

class ChannelPagerAdapter(
    private val context: Context,
    fm: FragmentManager,
    private val args: Bundle
) : ItemAwareFragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence {
        return context.getString(R.string.chat)
    }

    override fun getItem(position: Int): Fragment {
        return ChatFragment.newInstance(
            args.getString(C.CHANNEL_ID),
            args.getString(C.CHANNEL_LOGIN),
            args.getString(C.CHANNEL_DISPLAYNAME)
        )
    }

    override fun getCount(): Int = 1
}
