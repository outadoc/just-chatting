package fr.outadoc.justchatting.feature.pronouns.data

import fr.outadoc.justchatting.feature.pronouns.domain.PronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds

internal class AlejoPronounsApi(
    private val alejoPronounsClient: AlejoPronounsClient,
) : PronounsApi {

    override suspend fun getPronouns(): Result<List<Pronoun>> {
        return alejoPronounsClient
            .getPronouns()
            .map { pronouns ->
                pronouns.values.map { alejoPronoun ->
                    Pronoun(
                        id = alejoPronoun.id,
                        nominative = alejoPronoun.nominative,
                        objective = alejoPronoun.objective,
                        isSingular = alejoPronoun.isSingular,
                    )
                }
            }
    }

    override suspend fun getUserPronouns(userId: String): Result<UserPronounIds> {
        return alejoPronounsClient
            .getPronounsForUser(userId)
            .map { response ->
                UserPronounIds(
                    userId = userId,
                    mainPronounId = response.pronounId,
                    altPronounId = response.altPronounId,
                )
            }
    }
}
