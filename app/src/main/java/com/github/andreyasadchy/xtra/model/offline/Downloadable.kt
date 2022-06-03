package com.github.andreyasadchy.xtra.model.offline

interface Downloadable {
    val id: String
    val title: String?
    val thumbnail: String?
    val channelId: String?
    val channelLogin: String?
    val channelName: String?
    val channelLogo: String?
    val gameId: String?
    val gameName: String?
    val uploadDate: String?
    val type: String?
}
