package com.grigorevmp.secureui

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics

/**
 * Strips all Compose semantics from this node and its descendants unconditionally.
 *
 * Applies [clearAndSetSemantics] with an empty lambda, which removes every semantic
 * property (text, content description, actions, etc.) from the Compose accessibility
 * tree. Accessibility services will see this subtree as an empty, opaque node.
 *
 * Use the overload [secureSemantics(enabled)] when the stripping should be conditional.
 *
 * ```kotlin
 * Column(modifier = Modifier.secureSemantics()) {
 *     Text("This text will be invisible to accessibility services")
 * }
 * ```
 */
fun Modifier.secureSemantics(): Modifier = clearAndSetSemantics { }

/**
 * Conditionally strips all Compose semantics from this node.
 *
 * When [enabled] is `true`, behaves identically to [secureSemantics] (no-arg overload).
 * When `false`, returns the modifier chain unchanged — semantics remain intact.
 *
 * ```kotlin
 * val hidden by rememberSecureContentHidden()
 * Column(modifier = Modifier.secureSemantics(hidden)) { /* ... */ }
 * ```
 *
 * @param enabled `true` to clear semantics, `false` to keep them.
 */
fun Modifier.secureSemantics(enabled: Boolean): Modifier {
    return if (enabled) {
        secureSemantics()
    } else {
        this
    }
}

/**
 * Remembers and returns the process-wide [SecurityModeStore] singleton.
 *
 * The store is keyed on the application context, so it survives recomposition and
 * configuration changes. Safe to call from any composable.
 *
 * ```kotlin
 * val store = rememberSecurityModeStore()
 * store.setContentHidden(true)
 * ```
 */
@Composable
fun rememberSecurityModeStore(): SecurityModeStore {
    val context = LocalContext.current
    return remember(context.applicationContext) {
        SecurityModeStore.get(context)
    }
}

/**
 * Collects [SecurityModeStore.isContentHidden] as Compose [State].
 *
 * Returns a [State]`<Boolean>` that recomposes the calling composable whenever the
 * secure-mode toggle changes.
 *
 * ```kotlin
 * val hidden by rememberSecureContentHidden()
 * if (hidden) { /* show placeholder */ }
 * ```
 */
@Composable
fun rememberSecureContentHidden(): State<Boolean> {
    val store = rememberSecurityModeStore()
    return store.isContentHidden.collectAsState()
}

/**
 * Applies the view-level [SecureUiPolicy] to the host Android [View] that backs the
 * current Compose hierarchy, and sets [android.view.WindowManager.LayoutParams.FLAG_SECURE]
 * on the host activity's window.
 *
 * This is the primary entry point for Compose screens. Place it at the top of your
 * composable tree — it uses [DisposableEffect] keyed on the current view, hidden state,
 * and config, so the policy is reapplied whenever any of those change.
 *
 * If the composable is not hosted inside a [ComponentActivity] (e.g. inside a Dialog or
 * a standalone [androidx.compose.ui.platform.ComposeView]), the window-level flag is
 * skipped and only view-tree protections are applied.
 *
 * ```kotlin
 * @Composable
 * fun PaymentScreen() {
 *     ApplySecureComposeHostPolicy()
 *     val hidden by rememberSecureContentHidden()
 *     Column(modifier = Modifier.secureSemantics(hidden)) { /* ... */ }
 * }
 * ```
 *
 * @param config Granular toggle for each protection layer. Defaults to all-enabled.
 */
@Composable
fun ApplySecureComposeHostPolicy(
    config: SecureScreenConfig = SecureScreenConfig(),
) {
    val hidden = rememberSecureContentHidden().value
    val view = LocalView.current

    DisposableEffect(view, hidden, config) {
        view.applySecurePolicy(enabled = hidden, config = config)
        view.context.findActivity()?.let { activity ->
            SecureUiPolicy.applyToWindow(
                window = activity.window,
                enabled = hidden,
                blockScreenshots = config.blockScreenshots,
            )
        }
        onDispose { }
    }
}

/**
 * Walks up the [ContextWrapper] chain to find the hosting [ComponentActivity],
 * or returns `null` if no activity is in the chain.
 */
private fun Context.findActivity(): ComponentActivity? {
    var currentContext: Context? = this
    while (currentContext is ContextWrapper) {
        if (currentContext is ComponentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}
