package fr.outadoc.justchatting.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fr.outadoc.justchatting.ui.chat.ChannelChatFragment
import fr.outadoc.justchatting.ui.follow.FollowMediaFragment
import fr.outadoc.justchatting.ui.follow.channels.FollowedChannelsFragment
import fr.outadoc.justchatting.ui.search.SearchFragment
import fr.outadoc.justchatting.ui.search.channels.ChannelSearchFragment
import fr.outadoc.justchatting.ui.settings.SettingsActivity
import fr.outadoc.justchatting.ui.streams.followed.FollowedStreamsFragment
import fr.outadoc.justchatting.ui.view.chat.EmotesFragment
import fr.outadoc.justchatting.ui.view.chat.MessageClickedDialog
import fr.outadoc.justchatting.ui.view.chat.StreamInfoDialog

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
    abstract fun contributeEmotesFragment(): EmotesFragment

    @ContributesAndroidInjector
    abstract fun contributeChannelSearchFragment(): ChannelSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeChannelPagerFragment(): ChannelChatFragment

    @ContributesAndroidInjector
    abstract fun contributeMessageClickedDialog(): MessageClickedDialog

    @ContributesAndroidInjector
    abstract fun contributeStreamInfoDialog(): StreamInfoDialog

    @ContributesAndroidInjector
    abstract fun contributeFollowedChannelsFragment(): FollowedChannelsFragment

    @ContributesAndroidInjector
    abstract fun contributeFollowMediaFragment(): FollowMediaFragment
}
