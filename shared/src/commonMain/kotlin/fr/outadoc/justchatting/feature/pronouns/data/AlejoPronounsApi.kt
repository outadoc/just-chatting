package fr.outadoc.justchatting.feature.pronouns.data

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.pronouns.data.model.UserPronounResponse
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds
import io.ktor.client.plugins.ClientRequestException

internal class AlejoPronounsApi(
    private val alejoPronounsClient: AlejoPronounsClient,
) : PronounsApi {

    override suspend fun getPronouns(): Result<List<Pronoun>> {
        return alejoPronounsClient
            .getPronouns()
            .map { pronouns ->
                pronouns.map { alejoPronoun ->
                    Pronoun(
                        id = alejoPronoun.id,
                        nominative = alejoPronoun.nominative,
                        objective = alejoPronoun.objective,
                        isSingular = alejoPronoun.isSingular,
                    )
                }
            }
    }

    override suspend fun getUserPronouns(chatter: Chatter): Result<UserPronounIds> {
        return alejoPronounsClient
            .getPronounsForUser(chatter.login)
            .map { response: List<UserPronounResponse> ->
                val data = response.firstOrNull()
                UserPronounIds(
                    userId = chatter.id,
                    mainPronounId = data?.pronounId,
                    altPronounId = null,
                )
            }
            .recoverCatching { exception ->
                if (exception is ClientRequestException && exception.response.status.value == 404) {
                    UserPronounIds(
                        userId = chatter.id,
                        mainPronounId = null,
                        altPronounId = null
                    )
                } else {
                    throw exception
                }
            }
    }
}
