package fr.outadoc.justchatting.utils.core

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

fun String.desc() = StringDesc.Raw(this)
fun StringResource.desc() = StringDesc.Resource(this)

sealed interface StringDesc {
    data class Raw(val value: String) : StringDesc
    data class Resource(val value: StringResource) : StringDesc
}

@Composable
fun stringResource(desc: StringDesc): String {
    return when (desc) {
        is StringDesc.Raw -> desc.value
        is StringDesc.Resource -> stringResource(desc.value)
    }
}

suspend fun StringResource.format(vararg formatArgs: Any): StringDesc {
    return getString(this, *formatArgs).desc()
}

suspend fun PluralStringResource.format(quantity: Int, vararg formatArgs: Any): StringDesc {
    return getPluralString(this, quantity, *formatArgs).desc()
}
