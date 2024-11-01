package fr.outadoc.justchatting.utils.resources

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

internal sealed interface StringDesc {

    @Composable
    fun localized(): String

    class Raw(val value: String) : StringDesc {

        @Composable
        override fun localized(): String {
            return value
        }
    }

    class Resource(
        private val resource: StringResource,
    ) : StringDesc {

        @Composable
        override fun localized(): String {
            return stringResource(resource)
        }
    }

    class Formatted(
        private val resource: StringResource,
        private val args: Array<out Any>,
    ) : StringDesc {

        @Composable
        override fun localized(): String {
            return stringResource(resource, *args)
        }
    }

    class Plural(
        private val resource: PluralStringResource,
        private val number: Int,
    ) : StringDesc {

        @Composable
        override fun localized(): String {
            return pluralStringResource(resource, number)
        }
    }

    class PluralFormatted(
        private val resource: PluralStringResource,
        private val number: Int,
        private val args: Array<out Any>,
    ) : StringDesc {

        @Composable
        override fun localized(): String {
            return pluralStringResource(resource, number, *args)
        }
    }
}

// Moko

internal fun String.desc(): StringDesc {
    return StringDesc.Raw(this)
}

internal fun StringResource.desc(): StringDesc {
    return StringDesc.Resource(this)
}

internal fun StringResource.desc(vararg args: Any): StringDesc {
    return StringDesc.Formatted(this, args)
}

internal fun PluralStringResource.desc(number: Int): StringDesc {
    return StringDesc.Plural(this, number)
}

internal fun PluralStringResource.desc(number: Int, vararg args: Any): StringDesc {
    return StringDesc.PluralFormatted(this, number, args)
}
