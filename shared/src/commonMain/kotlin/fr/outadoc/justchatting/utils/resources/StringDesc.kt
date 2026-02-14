package fr.outadoc.justchatting.utils.resources

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

internal sealed interface StringDesc {
    @Composable
    fun localized(): String

    class Raw(
        val value: String,
    ) : StringDesc {
        @Composable
        override fun localized(): String = value
    }

    class Resource(
        private val resource: StringResource,
    ) : StringDesc {
        @Composable
        override fun localized(): String = stringResource(resource)
    }

    class Formatted(
        private val resource: StringResource,
        private val args: Array<out Any>,
    ) : StringDesc {
        @Composable
        override fun localized(): String {
            val formattedArgs: Array<Any> =
                args
                    .map { desc ->
                        when (desc) {
                            is StringDesc -> desc.localized()
                            else -> desc
                        }
                    }.toTypedArray()

            return stringResource(
                resource = resource,
                *formattedArgs,
            )
        }
    }

    class Plural(
        private val resource: PluralStringResource,
        private val number: Int,
    ) : StringDesc {
        @Composable
        override fun localized(): String = pluralStringResource(
            resource = resource,
            quantity = number,
        )
    }

    class PluralFormatted(
        private val resource: PluralStringResource,
        private val number: Int,
        private val args: Array<out Any>,
    ) : StringDesc {
        @Composable
        override fun localized(): String {
            val formattedArgs: Array<Any> =
                args
                    .map { desc ->
                        when (desc) {
                            is StringDesc -> desc.localized()
                            else -> desc
                        }
                    }.toTypedArray()

            return pluralStringResource(
                resource = resource,
                quantity = number,
                *formattedArgs,
            )
        }
    }
}

internal fun String.desc(): StringDesc = StringDesc.Raw(this)

internal fun StringResource.desc(): StringDesc = StringDesc.Resource(this)

internal fun StringResource.desc(vararg args: Any): StringDesc = StringDesc.Formatted(this, args)

internal fun PluralStringResource.desc(number: Int): StringDesc = StringDesc.Plural(this, number)

internal fun PluralStringResource.desc(
    number: Int,
    vararg args: Any,
): StringDesc = StringDesc.PluralFormatted(this, number, args)
