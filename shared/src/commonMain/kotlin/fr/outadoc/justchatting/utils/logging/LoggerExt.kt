package fr.outadoc.justchatting.utils.logging

inline fun <reified T : Any> logVerbose(noinline content: () -> String) =
    Logger.v(T::class, content)

inline fun <reified T : Any> logDebug(noinline content: () -> String) =
    Logger.d(T::class, content)

inline fun <reified T : Any> logInfo(noinline content: () -> String) =
    Logger.i(T::class, content)

inline fun <reified T : Any> logWarning(noinline content: () -> String) =
    Logger.w(T::class, content)

inline fun <reified T : Any> logError(
    throwable: Throwable? = null,
    noinline content: () -> String,
) = Logger.e(T::class, throwable, content)
