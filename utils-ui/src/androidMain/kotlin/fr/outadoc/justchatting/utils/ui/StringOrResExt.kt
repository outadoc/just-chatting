package fr.outadoc.justchatting.utils.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import fr.outadoc.justchatting.utils.core.StringOrRes

@Composable
fun StringOrRes.asString(): String =
    when (this) {
        is StringOrRes.Literal -> value.toString()
        is StringOrRes.Resource -> stringResource(id = resId)
    }
