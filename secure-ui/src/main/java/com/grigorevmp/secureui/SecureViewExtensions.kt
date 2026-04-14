package com.grigorevmp.secureui

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * One-shot application of the view-tree [SecureUiPolicy] to this view and all its
 * descendants.
 *
 * Walks the subtree and applies every protection layer specified in [config].
 * This does **not** subscribe to [SecurityModeStore] — it applies the policy once with
 * the given [enabled] value and returns. For reactive behavior, use [bindSecurityMode].
 *
 * ```kotlin
 * rootView.applySecurePolicy(enabled = true)
 * ```
 *
 * @param enabled `true` to activate protection, `false` to restore defaults.
 * @param config  Granular toggle for each protection layer. Defaults to all-enabled.
 */
fun View.applySecurePolicy(
    enabled: Boolean = true,
    config: SecureScreenConfig = SecureScreenConfig(),
) {
    SecureUiPolicy.applyToViewTree(
        root = this,
        enabled = enabled,
        blockAccessibilityTree = config.blockAccessibilityTree,
        scrubAccessibilityPayload = config.scrubAccessibilityPayload,
        markAccessibilityDataSensitive = config.markAccessibilityDataSensitive,
        blockAssistAndAutofill = config.blockAssistAndAutofill,
        filterTouchesWhenObscured = config.filterTouchesWhenObscured,
    )
}

/**
 * Subscribes this view tree to the [SecurityModeStore] and re-applies [SecureUiPolicy]
 * every time the secure-mode toggle changes.
 *
 * The subscription is lifecycle-aware: it collects [SecurityModeStore.isContentHidden]
 * inside [repeatOnLifecycle] with [Lifecycle.State.STARTED], so the policy is only
 * active while the [owner] is at least started (and automatically cancelled when stopped).
 *
 * ```kotlin
 * // In a Fragment's onViewCreated:
 * binding.root.bindSecurityMode(owner = viewLifecycleOwner)
 * ```
 *
 * @param owner  The [LifecycleOwner] that controls the subscription lifetime —
 *               typically `viewLifecycleOwner` in a Fragment.
 * @param store  The [SecurityModeStore] to observe. Defaults to the process-wide singleton.
 * @param config Granular toggle for each protection layer. Defaults to all-enabled.
 */
fun View.bindSecurityMode(
    owner: LifecycleOwner,
    store: SecurityModeStore = SecurityModeStore.get(context),
    config: SecureScreenConfig = SecureScreenConfig(),
) {
    owner.lifecycleScope.launch {
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            store.isContentHidden.collect { hidden ->
                applySecurePolicy(enabled = hidden, config = config)
            }
        }
    }
}
