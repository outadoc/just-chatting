package com.github.andreyasadchy.xtra.model.helix.tag

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Tag(
    val id: String? = null,
    val name: String? = null,
    val scope: String? = null) : Parcelable