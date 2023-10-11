package fr.outadoc.justchatting.component.chatapi.domain.model

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.collections.immutable.persistentListOf

@Parcelize
data class Stream(
    val id: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val gameName: String? = null,
    val title: String,
    val viewerCount: Int,
    val startedAt: String,
    val profileImageURL: String? = null,
    val tags: List<String> = persistentListOf(),
) : Parcelable
