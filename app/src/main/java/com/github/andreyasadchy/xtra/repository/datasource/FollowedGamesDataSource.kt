package com.github.andreyasadchy.xtra.repository.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.paging.DataSource
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.andreyasadchy.xtra.api.HelixApi
import com.github.andreyasadchy.xtra.model.helix.game.Game
import com.github.andreyasadchy.xtra.repository.LocalFollowGameRepository
import com.github.andreyasadchy.xtra.util.DownloadUtils
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class FollowedGamesDataSource(
    private val localFollowsGame: LocalFollowGameRepository,
    private val gqlClientId: String?,
    private val helixClientId: String?,
    private val userToken: String?,
    private val userId: String,
    private val api: HelixApi,
    coroutineScope: CoroutineScope) : BasePositionalDataSource<Game>(coroutineScope) {
    private var offset: String? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Game>) {
        loadInitial(params, callback) {
            val list = mutableListOf<Game>()
            for (i in localFollowsGame.loadFollows()) {
                list.add(Game(id = i.game_id, name = i.game_name, box_art_url = i.boxArt, followLocal = true))
            }
/*            if (userId != "") {
                val get = api.getFollowedChannels(helixClientId, userToken, userId, 100, offset)
                if (get.data != null) {
                    for (i in get.data) {
                        val item = list.find { it.to_id == i.to_id }
                        if (item == null) {
                            i.followTwitch = true
                            list.add(i)
                        } else {
                            item.followTwitch = true
                        }
                    }
                    offset = get.pagination?.cursor
                }
            }*/
            list.sortBy { it.name }
            list
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Game>) {
        loadRange(params, callback) {
            val list = mutableListOf<Game>()
/*            if (offset != null && offset != "") {
                if (userId != "") {
                    val get = api.getFollowedChannels(helixClientId, userToken, userId, 100, offset)
                    if (get.data != null) {
                        for (i in get.data) {
                            val item = list.find { it.to_id == i.to_id }
                            if (item == null) {
                                i.followTwitch = true
                                list.add(i)
                            } else {
                                item.followTwitch = true
                            }
                        }
                        offset = get.pagination?.cursor
                    }
                }
            }*/
            list
        }
    }

    private fun updateLocalGame(context: Context, gameId: String, box_art_url: String) {
        GlobalScope.launch {
            try {
                try {
                    Glide.with(context)
                        .asBitmap()
                        .load(TwitchApiHelper.getTemplateUrl(box_art_url, "game"))
                        .into(object: CustomTarget<Bitmap>() {
                            override fun onLoadCleared(placeholder: Drawable?) {

                            }

                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                DownloadUtils.savePng(context, "box_art", gameId, resource)
                            }
                        })
                } catch (e: Exception) {

                }
                val downloadedLogo = File(context.filesDir.toString() + File.separator + "box_art" + File.separator + "${gameId}.png").absolutePath
                localFollowsGame.getFollowById(gameId)?.let { localFollowsGame.updateFollow(it.apply {
                    boxArt = downloadedLogo }) }
            } catch (e: Exception) {

            }
        }
    }

    class Factory(
        private val localFollowsGame: LocalFollowGameRepository,
        private val gqlClientId: String?,
        private val helixClientId: String?,
        private val userToken: String?,
        private val userId: String,
        private val api: HelixApi,
        private val coroutineScope: CoroutineScope) : BaseDataSourceFactory<Int, Game, FollowedGamesDataSource>() {

        override fun create(): DataSource<Int, Game> =
                FollowedGamesDataSource(localFollowsGame, gqlClientId, helixClientId, userToken, userId, api, coroutineScope).also(sourceLiveData::postValue)
    }
}
