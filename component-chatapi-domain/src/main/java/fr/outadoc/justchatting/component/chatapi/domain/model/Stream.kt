package fr.outadoc.justchatting.component.chatapi.domain.model

import android.os.Parcelable
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stream(
    val id: String? = null,
    val userId: String? = null,
    val userLogin: String? = null,
    val userName: String? = null,
    val gameId: String? = null,
    val gameName: String? = null,
    val type: String? = null,
    val title: String? = null,
    val viewerCount: Int? = null,
    val startedAt: String? = null,
    val profileImageURL: String? = null,
    val tags: List<String> = persistentListOf(),
) : Parcelable
