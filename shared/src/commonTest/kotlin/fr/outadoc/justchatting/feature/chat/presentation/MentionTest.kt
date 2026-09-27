package fr.outadoc.justchatting.feature.chat.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class MentionTest {
    @Test
    fun `mention with prefix matches login`() {
        assertTrue(isMentionOf(mention = "@outadoc", login = "outadoc"))
    }

    @Test
    fun `mention matches login regardless of case`() {
        assertTrue(isMentionOf(mention = "@OutaDoc", login = "outadoc"))
    }

    @Test
    fun `mention followed by punctuation matches login`() {
        assertTrue(isMentionOf(mention = "@outadoc,", login = "outadoc"))
        assertTrue(isMentionOf(mention = "@outadoc:", login = "outadoc"))
        assertTrue(isMentionOf(mention = "@outadoc!?", login = "outadoc"))
    }

    @Test
    fun `mention keeps underscores in login`() {
        assertTrue(isMentionOf(mention = "@some_user_", login = "some_user_"))
    }

    @Test
    fun `mention of another user does not match`() {
        assertFalse(isMentionOf(mention = "@outadoc2", login = "outadoc"))
        assertFalse(isMentionOf(mention = "@out", login = "outadoc"))
    }

    @Test
    fun `bare prefix does not match`() {
        assertFalse(isMentionOf(mention = "@", login = "outadoc"))
    }
}
