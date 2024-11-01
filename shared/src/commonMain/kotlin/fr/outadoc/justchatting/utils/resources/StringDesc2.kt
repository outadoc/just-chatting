package fr.outadoc.justchatting.utils.resources

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.format
import fr.outadoc.justchatting.utils.presentation.asString

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
            return resource.format().asString()
        }
    }

    class Formatted(
        private val resource: StringResource,
        private val args: Array<out Any>,
    ) : StringDesc2 {

        @Composable
        override fun localized(): String {
            return resource.format(*args).asString()
        }
    }

    class Plural(
        private val resource: PluralsResource,
        private val number: Int,
    ) : StringDesc2 {

        @Composable
        override fun localized(): String {
            return resource.format(number).asString()
        }
    }

    class PluralFormatted(
        private val resource: PluralsResource,
        private val number: Int,
        private val args: Array<out Any>,
    ) : StringDesc2 {

        @Composable
        override fun localized(): String {
            return resource.format(number, *args).asString()
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

internal fun PluralsResource.desc2(number: Int): StringDesc2 {
    return StringDesc2.Plural(this, number)
}

internal fun PluralsResource.desc2(number: Int, vararg args: Any): StringDesc2 {
    return StringDesc2.PluralFormatted(this, number, args)
}

