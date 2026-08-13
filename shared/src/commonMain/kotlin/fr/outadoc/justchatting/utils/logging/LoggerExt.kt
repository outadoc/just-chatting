package fr.outadoc.justchatting.utils.logging

public inline fun logVerbose(
    tag: String,
    noinline content: () -> String,
): Unit = Logger.println(Logger.Level.Verbose, tag, content)

public inline fun logDebug(
    tag: String,
    noinline content: () -> String,
): Unit = Logger.println(Logger.Level.Debug, tag, content)

public inline fun logInfo(
    tag: String,
    noinline content: () -> String,
): Unit = Logger.println(Logger.Level.Info, tag, content)

public inline fun logWarning(
    tag: String,
    noinline content: () -> String,
): Unit = Logger.println(Logger.Level.Warning, tag, content)

public inline fun logError(
    tag: String,
    throwable: Throwable? = null,
    noinline content: () -> String,
) {
    Logger.println(
        level = Logger.Level.Error,
        tag = tag,
        content =
            if (throwable != null) {
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

public inline fun <reified T : Any> logVerbose(noinline content: () -> String): Unit = logVerbose(T::class.simpleName!!, content)

public inline fun <reified T : Any> logDebug(noinline content: () -> String): Unit = logDebug(T::class.simpleName!!, content)

public inline fun <reified T : Any> logInfo(noinline content: () -> String): Unit = logInfo(T::class.simpleName!!, content)

public inline fun <reified T : Any> logWarning(noinline content: () -> String): Unit = logWarning(T::class.simpleName!!, content)

public inline fun <reified T : Any> logError(
    throwable: Throwable? = null,
    noinline content: () -> String,
): Unit = logError(T::class.simpleName!!, throwable, content)
