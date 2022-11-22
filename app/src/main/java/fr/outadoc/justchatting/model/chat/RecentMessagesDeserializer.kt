package fr.outadoc.justchatting.model.chat

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import fr.outadoc.justchatting.irc.ChatMessageParser
import fr.outadoc.justchatting.log.logDebug
import java.lang.reflect.Type

class RecentMessagesDeserializer(
    private val parser: ChatMessageParser
) : JsonDeserializer<RecentMessagesResponse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): RecentMessagesResponse {
        return RecentMessagesResponse(
            messages = json.asJsonObject
                .getAsJsonArray("messages")
                .mapNotNull { message -> message.asString.takeIf { !it.isNullOrBlank() } }
                .mapNotNull { message ->
                    logDebug<RecentMessagesDeserializer> { "RecentMsg: $message" }
                    parser.parse(message)
                }
        )
    }
}