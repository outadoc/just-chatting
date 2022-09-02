package fr.outadoc.justchatting.model.helix.tag

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tag(
    val id: String? = null,
    val name: String? = null,
    val scope: String? = null
) : Parcelable
