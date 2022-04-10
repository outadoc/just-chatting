package com.github.andreyasadchy.xtra.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.andreyasadchy.xtra.ui.channel.ChannelPagerViewModel
import com.github.andreyasadchy.xtra.ui.chat.ChatViewModel
import com.github.andreyasadchy.xtra.ui.clips.common.ClipsViewModel
import com.github.andreyasadchy.xtra.ui.common.GenericViewModelFactory
import com.github.andreyasadchy.xtra.ui.download.ClipDownloadViewModel
import com.github.andreyasadchy.xtra.ui.download.VideoDownloadViewModel
import com.github.andreyasadchy.xtra.ui.follow.channels.FollowedChannelsViewModel
import com.github.andreyasadchy.xtra.ui.follow.games.FollowedGamesViewModel
import com.github.andreyasadchy.xtra.ui.games.GamesViewModel
import com.github.andreyasadchy.xtra.ui.main.MainViewModel
import com.github.andreyasadchy.xtra.ui.player.clip.ClipPlayerViewModel
import com.github.andreyasadchy.xtra.ui.player.games.PlayerGamesViewModel
import com.github.andreyasadchy.xtra.ui.player.offline.OfflinePlayerViewModel
import com.github.andreyasadchy.xtra.ui.player.stream.StreamPlayerViewModel
import com.github.andreyasadchy.xtra.ui.player.video.VideoPlayerViewModel
import com.github.andreyasadchy.xtra.ui.saved.bookmarks.BookmarksViewModel
import com.github.andreyasadchy.xtra.ui.saved.downloads.DownloadsViewModel
import com.github.andreyasadchy.xtra.ui.search.channels.ChannelSearchViewModel
import com.github.andreyasadchy.xtra.ui.search.games.GameSearchViewModel
import com.github.andreyasadchy.xtra.ui.search.tags.TagSearchViewModel
import com.github.andreyasadchy.xtra.ui.settings.SettingsViewModel
import com.github.andreyasadchy.xtra.ui.streams.common.StreamsViewModel
import com.github.andreyasadchy.xtra.ui.streams.followed.FollowedStreamsViewModel
import com.github.andreyasadchy.xtra.ui.videos.channel.ChannelVideosViewModel
import com.github.andreyasadchy.xtra.ui.videos.followed.FollowedVideosViewModel
import com.github.andreyasadchy.xtra.ui.videos.game.GameVideosViewModel
import com.github.andreyasadchy.xtra.ui.view.chat.MessageClickedViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: GenericViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GamesViewModel::class)
    abstract fun bindGamesViewModel(gamesViewModel: GamesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StreamsViewModel::class)
    abstract fun bindStreamsViewModel(streamListViewModel: StreamsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowedStreamsViewModel::class)
    abstract fun bindFollowedStreamsViewModel(followedStreamsViewModel: FollowedStreamsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StreamPlayerViewModel::class)
    abstract fun bindStreamPlayerViewModel(streamPlayerViewModel: StreamPlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VideoPlayerViewModel::class)
    abstract fun bindVideoPlayerViewModel(videoPlayerViewModel: VideoPlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ClipPlayerViewModel::class)
    abstract fun bindClipPlayerViewModel(clipPlayerViewModel: ClipPlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OfflinePlayerViewModel::class)
    abstract fun bindOfflinePlayerViewModel(offlinePlayerViewModel: OfflinePlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChannelVideosViewModel::class)
    abstract fun bindChannelVideosViewModel(channelVideosViewModel: ChannelVideosViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowedVideosViewModel::class)
    abstract fun bindFollowedVideosViewModel(followedVideosViewModel: FollowedVideosViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameVideosViewModel::class)
    abstract fun bindGameVideosViewModel(gameVideosViewModel: GameVideosViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DownloadsViewModel::class)
    abstract fun bindDownloadsViewModel(downloadsViewModel: DownloadsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BookmarksViewModel::class)
    abstract fun bindBookmarksViewModel(bookmarksViewModel: BookmarksViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ClipsViewModel::class)
    abstract fun bindClipsViewModel(clipsViewModel: ClipsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VideoDownloadViewModel::class)
    abstract fun bindVideoDownloadViewModel(videoDownloadViewModel: VideoDownloadViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ClipDownloadViewModel::class)
    abstract fun bindClipDownloadViewModel(clipDownloadViewModel: ClipDownloadViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChannelSearchViewModel::class)
    abstract fun bindChannelSearchViewModel(channelSearchViewModel: ChannelSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameSearchViewModel::class)
    abstract fun bindGameSearchViewModel(gameSearchViewModel: GameSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TagSearchViewModel::class)
    abstract fun bindTagSearchViewModel(tagSearchViewModel: TagSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChannelPagerViewModel::class)
    abstract fun bindChannelPagerViewModel(channelPagerViewModel: ChannelPagerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MessageClickedViewModel::class)
    abstract fun bindMessageClickedViewModel(messageClickedViewModel: MessageClickedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    abstract fun bindChatViewModel(chatViewModel: ChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowedChannelsViewModel::class)
    abstract fun bindFollowedChannelsViewModel(followedChannelsViewModel: FollowedChannelsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FollowedGamesViewModel::class)
    abstract fun bindFollowedGamesViewModel(followedGamesViewModel: FollowedGamesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlayerGamesViewModel::class)
    abstract fun bindPlayerGamesViewModel(playerGamesViewModel: PlayerGamesViewModel): ViewModel
}
