package com.github.andreyasadchy.xtra.di

import com.github.andreyasadchy.xtra.ui.chat.ChannelChatFragment
import com.github.andreyasadchy.xtra.ui.follow.FollowPagerFragment
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsFragment
import com.github.andreyasadchy.xtra.ui.search.SearchFragment
import com.github.andreyasadchy.xtra.ui.search.channels.ChannelSearchFragment
import com.github.andreyasadchy.xtra.ui.settings.SettingsActivity
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsFragment
import com.github.andreyasadchy.xtra.ui.view.chat.MessageClickedDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
@Suppress("unused")
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsActivity.SettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeFollowedStreamsFragment(): FollowedStreamsFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeChannelSearchFragment(): ChannelSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeChannelPagerFragment(): ChannelChatFragment

    @ContributesAndroidInjector
    abstract fun contributeMessageClickedDialog(): MessageClickedDialog

    @ContributesAndroidInjector
    abstract fun contributeFollowedChannelsFragment(): FollowedChannelsFragment

    @ContributesAndroidInjector
    abstract fun contributeFollowPagerFragment(): FollowPagerFragment
}
