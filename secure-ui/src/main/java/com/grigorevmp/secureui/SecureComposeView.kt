package com.grigorevmp.secureui

import androidx.compose.ui.platform.ComposeView

/**
 * Applies the view-tree [SecureUiPolicy] to this [ComposeView].
 *
 * This is a convenience wrapper for scenarios where a [ComposeView] is embedded inside a
 * traditional XML layout and you need to protect its subtree imperatively (outside of
 * a `@Composable` scope). For pure-Compose screens, prefer [ApplySecureComposeHostPolicy]
 * instead.
 *
 * The default [enabled] value is read from [SecurityModeStore.isContentHidden] at call
 * time, so a one-shot application matches the current global state. For reactive updates,
 * pair this with [View.bindSecurityMode][android.view.View.bindSecurityMode] or use the
 * Compose-level [ApplySecureComposeHostPolicy].
 *
 * ```kotlin
 * val composeView = findViewById<ComposeView>(R.id.myComposeView)
 * composeView.applySecureComposePolicy()
 * ```
 *
 * @param enabled `true` to activate protection, `false` to restore defaults.
 *                Defaults to the current [SecurityModeStore.isContentHidden] value.
 * @param config  Granular toggle for each protection layer. Defaults to all-enabled.
 */
fun ComposeView.applySecureComposePolicy(
    enabled: Boolean = SecurityModeStore.get(context).isContentHidden.value,
    config: SecureScreenConfig = SecureScreenConfig(),
) {
    applySecurePolicy(enabled = enabled, config = config)
}
