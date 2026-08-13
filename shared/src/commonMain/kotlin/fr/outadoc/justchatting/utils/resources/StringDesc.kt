package fr.outadoc.justchatting.utils.resources

import androidx.compose.runtime.Composable
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

public sealed interface StringDesc {
    @Composable
    public fun localized(): String

    public fun localizedString(): String

    public class Raw(
        public val value: String,
    ) : StringDesc {
        @Composable
        override fun localized(): String = value

        override fun localizedString(): String = value
    }

    public class Resource(
        private val resource: StringResource,
    ) : StringDesc {
        @Composable
        override fun localized(): String = stringResource(resource)

        override fun localizedString(): String = runBlocking { getString(resource) }
    }

    public class Formatted(
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

        override fun localizedString(): String =
            runBlocking {
                val formattedArgs: Array<Any> =
                    args
                        .map { desc ->
                            when (desc) {
                                is StringDesc -> desc.localizedString()
                                else -> desc
                            }
                        }.toTypedArray()
                getString(resource = resource, *formattedArgs)
            }
    }

    public class Plural(
        private val resource: PluralStringResource,
        private val number: Int,
    ) : StringDesc {
        @Composable
        override fun localized(): String =
            pluralStringResource(
                resource = resource,
                quantity = number,
            )

        override fun localizedString(): String = runBlocking { getPluralString(resource, number) }
    }

    public class PluralFormatted(
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

        override fun localizedString(): String =
            runBlocking {
                val formattedArgs: Array<Any> =
                    args
                        .map { desc ->
                            when (desc) {
                                is StringDesc -> desc.localizedString()
                                else -> desc
                            }
                        }.toTypedArray()
                getPluralString(resource, number, *formattedArgs)
            }
    }
}

public fun String.desc(): StringDesc = StringDesc.Raw(this)

public fun StringResource.desc(): StringDesc = StringDesc.Resource(this)

public fun StringResource.desc(vararg args: Any): StringDesc = StringDesc.Formatted(this, args)

public fun PluralStringResource.desc(number: Int): StringDesc = StringDesc.Plural(this, number)

public fun PluralStringResource.desc(
    number: Int,
    vararg args: Any,
): StringDesc = StringDesc.PluralFormatted(this, number, args)
