package com.github.andreyasadchy.xtra.model.chat

import com.google.gson.*
import java.lang.reflect.Type

class TwitchBadgesDeserializer : JsonDeserializer<TwitchBadgesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TwitchBadgesResponse {
        val gson = Gson()
        val badgeSets = json.asJsonObject.getAsJsonObject("badge_sets")
        return if (badgeSets.size() > 0) {
            val map = LinkedHashMap<Pair<String, String>, TwitchBadge>(badgeSets.size())
            for ((name) in badgeSets.entrySet()) {
                val versions = badgeSets.get(name).asJsonObject.getAsJsonObject("versions")
                for ((key, value) in versions.entrySet()) {
                    map[Pair(name, key)] = gson.fromJson(value.asJsonObject, TwitchBadge::class.java)
                }
            }
            TwitchBadgesResponse(map)
        } else {
            TwitchBadgesResponse()
        }
    }
}
