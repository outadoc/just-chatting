package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.shared.domain.model.User

public interface ChatNotifier {
    public val areNotificationsEnabled: Boolean

    /**
     * What the system currently lets this app do with notification bubbles.
     *
     * Bubbles are gated by an OS-level, per-app setting that the app can read but not change. When
     * it isn't [BubblePermission.AllConversations], bubbles silently don't appear, so this is
     * surfaced in the settings screen rather than left to guesswork.
     */
    public val bubblePermission: BubblePermission

    public fun notify(user: User)
}

public enum class BubblePermission {
    /** This platform has no notion of notification bubbles. */
    Unsupported,

    /** Bubbles are turned off, either globally or for this app. */
    Disabled,

    /**
     * Only conversations the user has individually opted in can bubble. This app posts every chat on
     * a single notification channel and an app cannot mark its own channel as bubble-enabled, so in
     * practice bubbles will not appear while this is set.
     */
    SelectedConversationsOnly,

    /** Every conversation this app posts may bubble. */
    AllConversations,
}
