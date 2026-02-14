package fr.outadoc.justchatting.feature.preferences.presentation.mobile

internal class DesktopAppVersionNameProvider : AppVersionNameProvider {
    override val appVersionName: String
        get() {
            val stream =
                javaClass.getResourceAsStream("/version.txt")
                    ?: error("version.txt not found in resources")

            return stream
                .bufferedReader()
                .readText()
                .takeIf { it.isNotBlank() }
                ?.trim()
                ?: "SNAPSHOT"
        }
}
