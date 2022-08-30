package fr.outadoc.justchatting.model.chat

class BttvEmote(
    val id: String,
    override val name: String
) : Emote() {

    companion object {
        private val ZERO_WIDTH_EMOTES = setOf(
            "SoSnowy",
            "IceCold",
            "SantaHat",
            "TopHat",
            "ReinDeer",
            "CandyCane",
            "cvMask",
            "cvHazmat"
        )
    }

    override fun getUrl(animate: Boolean, screenDensity: Float, isDarkTheme: Boolean): String {
        val availableDensities = listOf(
            1.0 to "1x",
            2.0 to "2x",
            3.0 to "3x"
        )

        val closest: String = availableDensities
            .minByOrNull { density -> screenDensity - density.first }
            ?.second
            ?: "1x"

        return "https://cdn.betterttv.net/emote/$id/$closest"
    }

    override val isZeroWidth: Boolean
        get() = name in ZERO_WIDTH_EMOTES
}
