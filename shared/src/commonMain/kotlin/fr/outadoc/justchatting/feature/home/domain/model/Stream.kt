package fr.outadoc.justchatting.feature.home.domain.model

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.utils.parcel.Parcelable
import fr.outadoc.justchatting.utils.parcel.Parcelize
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
@Parcelize
internal data class Stream(
    val id: String,
    val user: User,
    val gameName: String? = null,
    val title: String,
    val viewerCount: Int,
    val startedAt: String,
    val tags: ImmutableList<String> = persistentListOf(),
) : Parcelable
