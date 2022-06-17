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

    suspend fun loadFollowUser(
        clientId: String?,
        token: String?,
        userId: String?
    ): FollowUserDataResponse {
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
                            addProperty(
                                "sha256Hash",
                                "800e7346bdf7e5278a3c1d3f21b2b56e2639928f86815677a7126b093b2fdd08"
                            )
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
                            addProperty(
                                "sha256Hash",
                                "f7dae976ebf41c755ae2d758546bfd176b4eeb856656098bb40e0a672ca0d880"
                            )
                        }
                    )
                }
            )
        }
        return graphQL.getUnfollowUser(clientId, token, json)
    }
}
