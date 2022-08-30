package fr.outadoc.justchatting.model.id

import com.google.gson.annotations.SerializedName

class ValidationResponse(
    @SerializedName("client_id")
    val clientId: String,
    val login: String,
    @SerializedName("user_id")
    val userId: String
)
