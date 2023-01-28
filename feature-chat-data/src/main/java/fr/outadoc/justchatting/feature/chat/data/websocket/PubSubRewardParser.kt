package fr.outadoc.justchatting.feature.chat.data.websocket

import fr.outadoc.justchatting.feature.chat.data.model.PointReward
import kotlinx.datetime.Clock
import kotlinx.datetime.toInstant
import org.json.JSONObject

class PubSubRewardParser(private val clock: Clock) {

    fun parse(text: String): PointReward {
        val data = if (text.isNotBlank()) JSONObject(text).optJSONObject("data") else null
        val message = data?.optString("message")
            ?.let { if (it.isNotBlank() && !data.isNull("message")) JSONObject(it) else null }
        val messageData = message?.optString("data")
            ?.let { if (it.isNotBlank() && !message.isNull("data")) JSONObject(it) else null }
        val redemption = messageData?.optString("redemption")
            ?.let { if (it.isNotBlank() && !messageData.isNull("redemption")) JSONObject(it) else null }
        val user =
            redemption?.optString("user")?.let { if (it.isNotBlank()) JSONObject(it) else null }
        val reward = redemption?.optString("reward")
            ?.let { if (it.isNotBlank() && !redemption.isNull("reward")) JSONObject(it) else null }
        val rewardImage = reward?.optString("image")
            ?.let { if (it.isNotBlank() && !reward.isNull("image")) JSONObject(it) else null }
        val defaultImage = reward?.optString("default_image")
            ?.let { if (it.isNotBlank() && !reward.isNull("default_image")) JSONObject(it) else null }
        val input = redemption?.optString("user_input")

        return PointReward(
            id = reward?.optString("id"),
            userId = user?.optString("id"),
            userLogin = user?.optString("login"),
            userName = user?.optString("display_name"),
            message = input,
            rewardTitle = reward?.optString("title"),
            rewardCost = reward?.optInt("cost"),
            rewardImage = PointReward.RewardImage(
                url1 = rewardImage?.optString("url_1x") ?: defaultImage?.optString("url_1x"),
                url2 = rewardImage?.optString("url_2x") ?: defaultImage?.optString("url_2x"),
                url4 = rewardImage?.optString("url_4x") ?: defaultImage?.optString("url_4x"),
            ),
            timestamp = messageData?.optString("timestamp")?.toInstant() ?: clock.now(),
        )
    }
}
