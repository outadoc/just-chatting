package com.github.andreyasadchy.xtra.ui.clips.common

import android.app.Application
import android.content.Context
import androidx.core.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.model.offline.SortChannel
import com.github.andreyasadchy.xtra.model.offline.SortGame
import com.github.andreyasadchy.xtra.repository.*
import com.github.andreyasadchy.xtra.type.ClipsPeriod
import com.github.andreyasadchy.xtra.type.Language
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import com.github.andreyasadchy.xtra.ui.common.follow.FollowLiveData
import com.github.andreyasadchy.xtra.ui.common.follow.FollowViewModel
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ClipsViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService,
        private val localFollowsGame: LocalFollowGameRepository,
        private val sortChannelRepository: SortChannelRepository,
        private val sortGameRepository: SortGameRepository) : PagedListViewModel<Clip>(), FollowViewModel {

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
    val saveSort: Boolean
        get() = filter.value?.saveSort == true

    fun loadClips(context: Context, channelId: String? = null, channelLogin: String? = null, gameId: String? = null, gameName: String? = null, helixClientId: String? = null, helixToken: String? = null, gqlClientId: String? = null, channelApiPref: ArrayList<Pair<Long?, String?>?>, gameApiPref: ArrayList<Pair<Long?, String?>?>) {
        if (filter.value == null) {
            var sortValuesGame: SortGame? = null
            var sortValuesChannel: SortChannel? = null
            if (!gameId.isNullOrBlank() || !gameName.isNullOrBlank()) {
                sortValuesGame = gameId?.let { runBlocking { sortGameRepository.getById(it) } }
                if (sortValuesGame?.saveSort != true) {
                    sortValuesGame = runBlocking { sortGameRepository.getById("default") }
                }
            } else {
                if (!channelId.isNullOrBlank() || !channelLogin.isNullOrBlank()) {
                    sortValuesChannel = channelId?.let { runBlocking { sortChannelRepository.getById(it) } }
                    if (sortValuesChannel?.saveSort != true) {
                        sortValuesChannel = runBlocking { sortChannelRepository.getById("default") }
                    }
                }
            }
            filter.value = Filter(
                channelId = channelId,
                channelLogin = channelLogin,
                gameId = gameId,
                gameName = gameName,
                helixClientId = helixClientId,
                helixToken = helixToken,
                gqlClientId = gqlClientId,
                channelApiPref = channelApiPref,
                gameApiPref = gameApiPref,
                saveSort = sortValuesGame?.saveSort ?: sortValuesChannel?.saveSort,
                period = when (sortValuesGame?.clipPeriod ?: sortValuesChannel?.clipPeriod) {
                    Period.DAY.value -> Period.DAY
                    Period.MONTH.value -> Period.MONTH
                    Period.ALL.value -> Period.ALL
                    else -> Period.WEEK
                },
                languageIndex = sortValuesGame?.clipLanguageIndex ?: 0
            )
            _sortText.value = context.getString(R.string.sort_and_period, context.getString(R.string.view_count),
                when (sortValuesGame?.clipPeriod ?: sortValuesChannel?.clipPeriod) {
                    Period.DAY.value -> context.getString(R.string.today)
                    Period.MONTH.value -> context.getString(R.string.this_month)
                    Period.ALL.value -> context.getString(R.string.all_time)
                    else -> context.getString(R.string.this_week)
                }
            )
        } else {
            filter.value?.copy(channelId = channelId, channelLogin = channelLogin, gameId = gameId, gameName = gameName, helixClientId = helixClientId, helixToken = helixToken, gqlClientId = gqlClientId, channelApiPref = channelApiPref, gameApiPref = gameApiPref).let {
                if (filter.value != it)
                    filter.value = it
            }
        }
    }

    fun filter(period: Period, languageIndex: Int, text: CharSequence, saveSort: Boolean) {
        filter.value = filter.value?.copy(saveSort = saveSort, period = period, languageIndex = languageIndex)
        _sortText.value = text
        viewModelScope.launch {
            if (!filter.value?.gameId.isNullOrBlank() || !filter.value?.gameName.isNullOrBlank()) {
                val sortValues = filter.value?.gameId?.let { sortGameRepository.getById(it) }
                if (saveSort) {
                    (sortValues?.apply {
                        this.saveSort = saveSort
                        clipPeriod = period.value
                        clipLanguageIndex = languageIndex
                    } ?: filter.value?.gameId?.let { SortGame(
                        id = it,
                        saveSort = saveSort,
                        clipPeriod = period.value,
                        clipLanguageIndex = languageIndex)
                    })?.let { sortGameRepository.save(it) }
                } else {
                    (sortValues?.apply {
                        this.saveSort = saveSort
                    } ?: filter.value?.gameId?.let { SortGame(
                        id = it,
                        saveSort = saveSort)
                    })?.let { sortGameRepository.save(it) }
                    val sortDefaults = sortGameRepository.getById("default")
                    (sortDefaults?.apply {
                        clipPeriod = period.value
                        clipLanguageIndex = languageIndex
                    } ?: SortGame(
                        id = "default",
                        clipPeriod = period.value,
                        clipLanguageIndex = languageIndex
                    )).let { sortGameRepository.save(it) }
                }
            } else {
                if (!filter.value?.channelId.isNullOrBlank() || !filter.value?.channelLogin.isNullOrBlank()) {
                    val sortValues = filter.value?.channelId?.let { sortChannelRepository.getById(it) }
                    if (saveSort) {
                        (sortValues?.apply {
                            this.saveSort = saveSort
                            clipPeriod = period.value
                        } ?: filter.value?.channelId?.let { SortChannel(
                            id = it,
                            saveSort = saveSort,
                            clipPeriod = period.value)
                        })?.let { sortChannelRepository.save(it) }
                    } else {
                        (sortValues?.apply {
                            this.saveSort = saveSort
                        } ?: filter.value?.channelId?.let { SortChannel(
                            id = it,
                            saveSort = saveSort)
                        })?.let { sortChannelRepository.save(it) }
                        val sortDefaults = sortChannelRepository.getById("default")
                        (sortDefaults?.apply {
                            clipPeriod = period.value
                        } ?: SortChannel(
                            id = "default",
                            clipPeriod = period.value
                        )).let { sortChannelRepository.save(it) }
                    }
                }
            }
        }
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
        val saveSort: Boolean?,
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

    override fun setUser(user: User, helixClientId: String?, gqlClientId: String?, setting: Int) {
        if (!this::follow.isInitialized) {
            follow = FollowLiveData(localFollowsGame = localFollowsGame, userId = userId, userLogin = userLogin, userName = userName, channelLogo = channelLogo, repository = repository, helixClientId = helixClientId, user = user, gqlClientId = gqlClientId, setting = setting, viewModelScope = viewModelScope)
        }
    }
}
