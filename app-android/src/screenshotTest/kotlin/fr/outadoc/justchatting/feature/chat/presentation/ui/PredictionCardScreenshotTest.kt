package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.Prediction
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private val mockPrediction =
    Prediction(
        id = "1234",
        status = Prediction.Status.Resolved,
        title = "QUI GAGNE ?",
        createdAt = Instant.parse("2023-02-05T18:11:52.832Z"),
        predictionWindow = 5.minutes,
        outcomes =
            listOf(
                Prediction.Outcome(
                    id = "1",
                    title = "Étoiles",
                    totalPoints = 12345,
                    totalUsers = 1000,
                    badge = Badge(id = "123", version = "5"),
                    color = "#00FF00",
                ),
                Prediction.Outcome(
                    id = "1",
                    title = "AntoineDaniel",
                    totalPoints = 102345,
                    totalUsers = 1000,
                    badge = Badge(id = "123", version = "5"),
                    color = "#FF0000",
                ),
                Prediction.Outcome(
                    id = "1",
                    title = "HortyUnderscore",
                    totalPoints = 52450,
                    totalUsers = 1000,
                    badge = Badge(id = "123", version = "5"),
                    color = "#0000FF",
                ),
            ),
    )

@PreviewTest
@Preview
@Composable
internal fun PredictionCardScreenshotTest() {
    AppTheme {
        PredictionCard(
            prediction = mockPrediction.copy(status = Prediction.Status.Active),
        )
    }
}

@PreviewTest
@Preview
@Composable
internal fun PredictionCardCompletedScreenshotTest() {
    AppTheme {
        PredictionCard(
            prediction = mockPrediction.copy(status = Prediction.Status.Resolved),
        )
    }
}
