package fr.outadoc.justchatting.feature.auth.domain.model

/**
 * Thrown when Twitch explicitly rejects a token as invalid or expired, as opposed to the
 * validation request failing for transient reasons (network outage, timeout, server error).
 */
internal class InvalidTokenException(
    cause: Throwable? = null,
) : Exception("The token was rejected by Twitch", cause)
