package com.github.andreyasadchy.xtra.ui.clips.common

import android.app.Application
import androidx.core.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.LocalFollowGameRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.type.ClipsPeriod
import com.github.andreyasadchy.xtra.type.Language
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import com.github.andreyasadchy.xtra.ui.common.follow.FollowLiveData
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import javax.inject.Inject

class ClipsViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService,
        private val localFollowsGame: LocalFollowGameRepository) : PagedListViewModel<Clip>(), FollowViewModel {

    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Clip>> = Transformations.map(filter) {
        val started = when (it.period) {
            Period.ALL -> null
            else -> TwitchApiHelper.getClipTime(it.period)
        }
        val ended = when (it.period) {
            Period.ALL -> null
            else -> TwitchApiHelper.getClipTime()
        }
        val gqlQueryPeriod = when (it.period) {
            Period.DAY -> ClipsPeriod.LAST_DAY
            Period.WEEK -> ClipsPeriod.LAST_WEEK
            Period.MONTH -> ClipsPeriod.LAST_MONTH
            else -> ClipsPeriod.ALL_TIME }
        val gqlPeriod = when (it.period) {
            Period.DAY -> "LAST_DAY"
            Period.WEEK -> "LAST_WEEK"
            Period.MONTH -> "LAST_MONTH"
            else -> "ALL_TIME" }
        if (it.gameId == null && it.gameName == null) {
            repository.loadChannelClips(it.channelId, it.channelLogin, it.helixClientId, it.helixToken, started, ended, it.gqlClientId,
                gqlQueryPeriod, gqlPeriod, it.channelApiPref, viewModelScope)
        } else {
            val langList = mutableListOf<Language>()
            val langValues = context.resources.getStringArray(R.array.gqlUserLanguageValues).toList()
            if (languageIndex != 0) {
                val item = Language.values().find { lang -> lang.rawValue == langValues.elementAt(languageIndex) }
                if (item != null) {
                    langList.add(item)
                }
            }
            repository.loadGameClips(it.gameId, it.gameName, it.helixClientId, it.helixToken, started, ended, it.gqlClientId, langList.ifEmpty { null },
                gqlQueryPeriod, gqlPeriod, it.gameApiPref, viewModelScope)
        }
    }
    val period: Period
        get() = filter.value!!.period
    val languageIndex: Int
        get() = filter.value!!.languageIndex

    init {
        _sortText.value = context.getString(R.string.sort_and_period, context.getString(R.string.view_count), context.getString(R.string.this_week))
    }

    fun loadClips(channelId: String? = null, channelLogin: String? = null, gameId: String? = null, gameName: String? = null, helixClientId: String? = null, helixToken: String? = null, gqlClientId: String? = null, channelApiPref: ArrayList<Pair<Long?, String?>?>, gameApiPref: ArrayList<Pair<Long?, String?>?>) {
        if (filter.value == null) {
            filter.value = Filter(channelId, channelLogin, gameId, gameName, helixClientId, helixToken, gqlClientId, channelApiPref, gameApiPref)
        } else {
            filter.value?.copy(channelId = channelId, channelLogin = channelLogin, gameId = gameId, gameName = gameName, helixClientId = helixClientId, helixToken = helixToken, gqlClientId = gqlClientId, channelApiPref = channelApiPref, gameApiPref = gameApiPref).let {
                if (filter.value != it)
                    filter.value = it
            }
        }
    }

    fun filter(period: Period, languageIndex: Int, text: CharSequence) {
        filter.value = filter.value?.copy(period = period, languageIndex = languageIndex)
        _sortText.value = text
    }

    private data class Filter(
        val channelId: String?,
        val channelLogin: String?,
        val gameId: String?,
        val gameName: String?,
        val helixClientId: String?,
        val helixToken: String?,
        val gqlClientId: String?,
        val channelApiPref: ArrayList<Pair<Long?, String?>?>,
        val gameApiPref: ArrayList<Pair<Long?, String?>?>,
        val period: Period = Period.WEEK,
        val languageIndex: Int = 0)

    override val userId: String?
        get() { return filter.value?.gameId }
    override val userLogin: String?
        get() = null
    override val userName: String?
        get() { return filter.value?.gameName }
    override val channelLogo: String?
        get() = null
    override val game: Boolean
        get() = true
    override lateinit var follow: FollowLiveData

    override fun setUser(user: User, helixClientId: String?, gqlClientId: String?) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(localFollowsGame = localFollowsGame, userId = userId, userLogin = userLogin, userName = userName, channelLogo = channelLogo, repository = repository, helixClientId = helixClientId, user = user, gqlClientId = gqlClientId, viewModelScope = viewModelScope)
        }
    }
}
