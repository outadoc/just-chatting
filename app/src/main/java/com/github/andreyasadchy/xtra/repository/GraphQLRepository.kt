package com.github.andreyasadchy.xtra.repository

import com.github.andreyasadchy.xtra.api.GraphQLApi
import com.github.andreyasadchy.xtra.model.chat.EmoteCardResponse
import com.github.andreyasadchy.xtra.model.gql.channel.ChannelViewerListDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowUserDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedChannelsDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowedStreamsDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowingGameDataResponse
import com.github.andreyasadchy.xtra.model.gql.followed.FollowingUserDataResponse
import com.github.andreyasadchy.xtra.model.gql.search.SearchChannelDataResponse
import com.github.andreyasadchy.xtra.model.gql.stream.ViewersDataResponse
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraphQLRepository @Inject constructor(private val graphQL: GraphQLApi) {

    suspend fun loadChannelViewerList(clientId: String?, channelLogin: String?): ChannelViewerListDataResponse {
        val json = JsonObject().apply {
            addProperty("operationName", "ChatViewers")
            add(
                "variables",
                JsonObject().apply {
                    addProperty("channelLogin", channelLogin)
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "e0761ef5444ee3acccee5cfc5b834cbfd7dc220133aa5fbefe1b66120f506250")
                        }
                    )
                }
            )
        }
        return graphQL.getChannelViewerList(clientId, json)
    }

    suspend fun loadSearchChannels(clientId: String?, query: String?, cursor: String?): SearchChannelDataResponse {
        val json = JsonObject().apply {
            addProperty("operationName", "SearchResultsPage_SearchResults")
            add(
                "variables",
                JsonObject().apply {
                    add(
                        "options",
                        JsonObject().apply {
                            add(
                                "targets",
                                JsonArray().apply {
                                    add(
                                        JsonObject().apply {
                                            addProperty("cursor", cursor)
                                            addProperty("index", "CHANNEL")
                                        }
                                    )
                                }
                            )
                        }
                    )
                    addProperty("query", query)
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "ee977ac21b324669b4c109be49ed3032227e8850bea18503d0ced68e8156c2a5")
                        }
                    )
                }
            )
        }
        return graphQL.getSearchChannels(clientId, json)
    }

    suspend fun loadViewerCount(clientId: String?, channel: String?): ViewersDataResponse {
        val json = JsonObject().apply {
            addProperty("operationName", "UseViewCount")
            add(
                "variables",
                JsonObject().apply {
                    addProperty("channelLogin", channel)
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "00b11c9c428f79ae228f30080a06ffd8226a1f068d6f52fbc057cbde66e994c2")
                        }
                    )
                }
            )
        }
        return graphQL.getViewerCount(clientId, json)
    }

    suspend fun loadEmoteCard(clientId: String?, emoteId: String?): EmoteCardResponse {
        val json = JsonObject().apply {
            addProperty("operationName", "EmoteCard")
            add(
                "variables",
                JsonObject().apply {
                    addProperty("emoteID", emoteId)
                    addProperty("octaneEnabled", true)
                    addProperty("artistEnabled", true)
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "556230dd63957761355ba54232c43f4781f31ed6686fc827053b9aa7b199848f")
                        }
                    )
                }
            )
        }
        return graphQL.getEmoteCard(clientId, json)
    }

    suspend fun loadFollowedStreams(clientId: String?, token: String?, limit: Int?, cursor: String?): FollowedStreamsDataResponse {
        val json = JsonObject().apply {
            addProperty("operationName", "FollowingLive_CurrentUser")
            add(
                "variables",
                JsonObject().apply {
                    addProperty("cursor", cursor)
                    addProperty("limit", limit)
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "40ac5a060fa06ba73e07bf8dd8c3cf6aca4494aeed2222c986ed47ffddf31f51")
                        }
                    )
                }
            )
        }
        return graphQL.getFollowedStreams(clientId, token, json)
    }

    suspend fun loadFollowedChannels(clientId: String?, token: String?, limit: Int?, cursor: String?): FollowedChannelsDataResponse {
        val json = JsonObject().apply {
            addProperty("operationName", "ChannelFollows")
            add(
                "variables",
                JsonObject().apply {
                    addProperty("cursor", cursor)
                    addProperty("limit", limit)
                    addProperty("order", "DESC")
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "4b9cb31b54b9213e5760f2f6e9e935ad09924cac2f78aac51f8a64d85f028ed0")
                        }
                    )
                }
            )
        }
        return graphQL.getFollowedChannels(clientId, token, json)
    }

    suspend fun loadFollowUser(clientId: String?, token: String?, userId: String?): FollowUserDataResponse {
        val json = JsonObject().apply {
            addProperty("operationName", "FollowButton_FollowUser")
            add(
                "variables",
                JsonObject().apply {
                    add(
                        "input",
                        JsonObject().apply {
                            addProperty("disableNotifications", false)
                            addProperty("targetID", userId)
                        }
                    )
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "800e7346bdf7e5278a3c1d3f21b2b56e2639928f86815677a7126b093b2fdd08")
                        }
                    )
                }
            )
        }
        return graphQL.getFollowUser(clientId, token, json)
    }

    suspend fun loadUnfollowUser(clientId: String?, token: String?, userId: String?): JsonElement {
        val json = JsonObject().apply {
            addProperty("operationName", "FollowButton_UnfollowUser")
            add(
                "variables",
                JsonObject().apply {
                    add(
                        "input",
                        JsonObject().apply {
                            addProperty("targetID", userId)
                        }
                    )
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "f7dae976ebf41c755ae2d758546bfd176b4eeb856656098bb40e0a672ca0d880")
                        }
                    )
                }
            )
        }
        return graphQL.getUnfollowUser(clientId, token, json)
    }

    suspend fun loadFollowGame(clientId: String?, token: String?, gameId: String?): JsonElement {
        val json = JsonObject().apply {
            addProperty("operationName", "FollowGameButton_FollowGame")
            add(
                "variables",
                JsonObject().apply {
                    add(
                        "input",
                        JsonObject().apply {
                            addProperty("gameID", gameId)
                        }
                    )
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "b846b65ba4bc9a3561dbe2d069d95deed9b9e031bcfda2482d1bedd84a1c2eb3")
                        }
                    )
                }
            )
        }
        return graphQL.getFollowGame(clientId, token, json)
    }

    suspend fun loadUnfollowGame(clientId: String?, token: String?, gameId: String?): JsonElement {
        val json = JsonObject().apply {
            addProperty("operationName", "FollowGameButton_UnfollowGame")
            add(
                "variables",
                JsonObject().apply {
                    add(
                        "input",
                        JsonObject().apply {
                            addProperty("gameID", gameId)
                        }
                    )
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "811e02e396ebba0664f21ff002f2eff3c6f57e8af9aedb4f4dfa77cefd0db43d")
                        }
                    )
                }
            )
        }
        return graphQL.getUnfollowGame(clientId, token, json)
    }

    suspend fun loadFollowingUser(clientId: String?, token: String?, userLogin: String?): FollowingUserDataResponse {
        val json = JsonObject().apply {
            addProperty("operationName", "ChannelSupportButtons")
            add(
                "variables",
                JsonObject().apply {
                    addProperty("channelLogin", userLogin)
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "834a75e1c06cffada00f0900664a5033e392f6fb655fae8d2e25b21b340545a9")
                        }
                    )
                }
            )
        }
        return graphQL.getFollowingUser(clientId, token, json)
    }

    suspend fun loadFollowingGame(clientId: String?, token: String?, gameName: String?): FollowingGameDataResponse {
        val json = JsonObject().apply {
            addProperty("operationName", "FollowGameButton_Game")
            add(
                "variables",
                JsonObject().apply {
                    addProperty("name", gameName)
                }
            )
            add(
                "extensions",
                JsonObject().apply {
                    add(
                        "persistedQuery",
                        JsonObject().apply {
                            addProperty("version", 1)
                            addProperty("sha256Hash", "cfeda60899b6b867b2d7f30c8556778c4a9cc8268bd1aadd9f88134a0f642a02")
                        }
                    )
                }
            )
        }
        return graphQL.getFollowingGame(clientId, token, json)
    }
}
