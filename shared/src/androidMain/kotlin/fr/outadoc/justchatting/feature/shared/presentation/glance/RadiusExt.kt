package fr.outadoc.justchatting.feature.shared.presentation.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius

@Composable
internal fun GlanceModifier.appCornerRadius(): GlanceModifier {
    if (android.os.Build.VERSION.SDK_INT >= 31) {
        val systemCornerRadiusDefined =
            LocalContext.current.resources.getResourceName(
                android.R.dimen.system_app_widget_background_radius
            ) != null

        if (systemCornerRadiusDefined) {
            return cornerRadius(android.R.dimen.system_app_widget_inner_radius)
        }
    }

    return cornerRadius(8.dp)
}
