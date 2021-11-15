package com.github.andreyasadchy.xtra.model.helix.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: String,
    val login: String,
    val display_name: String,
    val type: String,
    val broadcaster_type: String,
    val description: String?,
    val profile_image_url: String,
    val offline_image_url: String,
    val view_count: Int,
    val created_at: String) : Parcelable