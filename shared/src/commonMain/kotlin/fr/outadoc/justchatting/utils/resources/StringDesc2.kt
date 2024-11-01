package fr.outadoc.justchatting.utils.resources

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

internal sealed interface StringDesc2 {

    @Composable
    fun localized(): String

    class Raw(val value: String) : StringDesc2 {

        @Composable
        override fun localized(): String {
            return value
        }
    }

    class Resource(
        private val resource: StringResource,
    ) : StringDesc2 {

        @Composable
        override fun localized(): String {
            return stringResource(resource)
        }
    }

    class Formatted(
        private val resource: StringResource,
        private val args: Array<out Any>,
    ) : StringDesc2 {

        @Composable
        override fun localized(): String {
            return stringResource(resource, *args)
        }
    }

    class Plural(
        private val resource: PluralStringResource,
        private val number: Int,
    ) : StringDesc2 {

        @Composable
        override fun localized(): String {
            return pluralStringResource(resource, number)
        }
    }

    class PluralFormatted(
        private val resource: PluralStringResource,
        private val number: Int,
        private val args: Array<out Any>,
    ) : StringDesc2 {

        @Composable
        override fun localized(): String {
            return pluralStringResource(resource, number, *args)
        }
    }
}

// Moko

internal fun String.desc2(): StringDesc2 {
    return StringDesc2.Raw(this)
}

internal fun StringResource.desc2(): StringDesc2 {
    return StringDesc2.Resource(this)
}

internal fun StringResource.desc2(vararg args: Any): StringDesc2 {
    return StringDesc2.Formatted(this, args)
}

internal fun PluralStringResource.desc2(number: Int): StringDesc2 {
    return StringDesc2.Plural(this, number)
}

internal fun PluralStringResource.desc2(number: Int, vararg args: Any): StringDesc2 {
    return StringDesc2.PluralFormatted(this, number, args)
}
