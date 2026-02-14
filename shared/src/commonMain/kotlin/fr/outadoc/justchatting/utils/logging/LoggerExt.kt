package fr.outadoc.justchatting.utils.logging

internal inline fun logVerbose(tag: String, noinline content: () -> String) = Logger.println(Logger.Level.Verbose, tag, content)

internal inline fun logDebug(tag: String, noinline content: () -> String) = Logger.println(Logger.Level.Debug, tag, content)

internal inline fun logInfo(tag: String, noinline content: () -> String) = Logger.println(Logger.Level.Info, tag, content)

internal inline fun logWarning(tag: String, noinline content: () -> String) = Logger.println(Logger.Level.Warning, tag, content)

internal inline fun logError(
    tag: String,
    throwable: Throwable? = null,
    noinline content: () -> String,
) {
    Logger.println(
        level = Logger.Level.Error,
        tag = tag,
        content = if (throwable != null) {
            {
                buildString {
                    appendLine(content())
                    appendLine(throwable.stackTraceToString())
                }
            }
        } else {
            content
        },
    )
}

internal inline fun <reified T : Any> logVerbose(noinline content: () -> String) = logVerbose(T::class.simpleName!!, content)

internal inline fun <reified T : Any> logDebug(noinline content: () -> String) = logDebug(T::class.simpleName!!, content)

internal inline fun <reified T : Any> logInfo(noinline content: () -> String) = logInfo(T::class.simpleName!!, content)

internal inline fun <reified T : Any> logWarning(noinline content: () -> String) = logWarning(T::class.simpleName!!, content)

internal inline fun <reified T : Any> logError(
    throwable: Throwable? = null,
    noinline content: () -> String,
) {
    return logError(T::class.simpleName!!, throwable, content)
}
