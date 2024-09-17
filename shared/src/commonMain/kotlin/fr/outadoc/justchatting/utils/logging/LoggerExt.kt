package fr.outadoc.justchatting.utils.logging

internal inline fun <reified T : Any> logVerbose(noinline content: () -> String) =
    Logger.println(Logger.Level.Verbose, T::class.simpleName!!, content)

internal inline fun <reified T : Any> logDebug(noinline content: () -> String) =
    Logger.println(Logger.Level.Debug, T::class.simpleName!!, content)

internal inline fun <reified T : Any> logInfo(noinline content: () -> String) =
    Logger.println(Logger.Level.Info, T::class.simpleName!!, content)

internal inline fun <reified T : Any> logWarning(noinline content: () -> String) =
    Logger.println(Logger.Level.Warning, T::class.simpleName!!, content)

internal inline fun <reified T : Any> logError(
    throwable: Throwable? = null,
    noinline content: () -> String,
) {
    Logger.println(
        level = Logger.Level.Error,
        tag = T::class.simpleName!!,
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
