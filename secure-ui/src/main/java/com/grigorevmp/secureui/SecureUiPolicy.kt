package com.grigorevmp.secureui

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi

/**
 * Central policy engine that applies (or removes) all security protections to a window
 * and its view tree.
 *
 * Each protection layer can be toggled independently via the boolean flags in
 * [SecureScreenConfig]. The layers are:
 *
 * | Layer | What it does |
 * |---|---|
 * | Accessibility tree hiding | Sets [View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS] on the root |
 * | Accessibility payload scrubbing | Swaps [View.AccessibilityDelegate] on every view via [SecureAccessibilityScrubber] |
 * | Accessibility data sensitivity (API 34+) | Calls [View.setAccessibilityDataSensitive] |
 * | Assist / autofill blocking | Disables autofill hints and calls the hidden `View.setAssistBlocked` via reflection |
 * | Screenshot blocking | Adds [WindowManager.LayoutParams.FLAG_SECURE] to the host window |
 * | Overlay touch filtering | Enables [View.setFilterTouchesWhenObscured] on every view |
 *
 * All public functions are idempotent: calling them repeatedly with the same arguments
 * produces no additional side effects.
 */
object SecureUiPolicy {

    /**
     * Applies the full protection policy to [window] and [root] in one call.
     *
     * This is the main entry point used by [SecureActivity]. It delegates to
     * [applyToWindow] for screenshot protection and to [applyToViewTree] for everything
     * else.
     *
     * @param window The activity's [Window] — used for [WindowManager.LayoutParams.FLAG_SECURE].
     * @param root   The root view of the content hierarchy, typically `findViewById(android.R.id.content)`.
     *               If `null`, only window-level flags are applied.
     * @param enabled `true` to activate protection, `false` to restore defaults.
     * @param config  Granular toggle for each protection layer.
     */
    fun apply(
        window: Window,
        root: View?,
        enabled: Boolean,
        config: SecureScreenConfig,
    ) {
        applyToWindow(
            window = window,
            enabled = enabled,
            blockScreenshots = config.blockScreenshots,
        )
        root ?: return
        applyToViewTree(
            root = root,
            enabled = enabled,
            blockAccessibilityTree = config.blockAccessibilityTree,
            scrubAccessibilityPayload = config.scrubAccessibilityPayload,
            markAccessibilityDataSensitive = config.markAccessibilityDataSensitive,
            blockAssistAndAutofill = config.blockAssistAndAutofill,
            filterTouchesWhenObscured = config.filterTouchesWhenObscured,
        )
    }

    /**
     * Adds or removes [WindowManager.LayoutParams.FLAG_SECURE] on [window].
     *
     * When the flag is set the system prevents screenshots, screen recording,
     * and display on non-secure outputs (e.g. Chromecast mirroring).
     *
     * This is a no-op if [blockScreenshots] is `false`.
     *
     * @param window The target [Window].
     * @param enabled `true` to add the flag, `false` to clear it.
     * @param blockScreenshots Gate flag — if `false`, the method returns immediately.
     */
    fun applyToWindow(
        window: Window,
        enabled: Boolean,
        blockScreenshots: Boolean,
    ) {
        if (!blockScreenshots) {
            return
        }
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    /**
     * Walks the view tree rooted at [root] and applies all view-level protections.
     *
     * The protections applied depend on the boolean flags:
     *
     * - **blockAccessibilityTree** — sets [View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS]
     *   (or resets to [View.IMPORTANT_FOR_ACCESSIBILITY_AUTO]) on [root] only.
     * - **scrubAccessibilityPayload** — delegates to [SecureAccessibilityScrubber.applyRecursively]
     *   which replaces the [View.AccessibilityDelegate] on every view in the subtree.
     * - **markAccessibilityDataSensitive** — on API 34+ calls
     *   [View.setAccessibilityDataSensitive] on [root].
     * - **blockAssistAndAutofill** — on API 26+ sets
     *   [View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS] and clears autofill hints;
     *   on API 23+ calls the hidden `View.setAssistBlocked(Boolean)` via reflection.
     * - **filterTouchesWhenObscured** — sets [View.setFilterTouchesWhenObscured] on every
     *   view in the subtree.
     *
     * @param root The root view to start traversal from.
     * @param enabled `true` to activate protection, `false` to restore defaults.
     * @param blockAccessibilityTree See [SecureScreenConfig.blockAccessibilityTree].
     * @param scrubAccessibilityPayload See [SecureScreenConfig.scrubAccessibilityPayload].
     * @param markAccessibilityDataSensitive See [SecureScreenConfig.markAccessibilityDataSensitive].
     * @param blockAssistAndAutofill See [SecureScreenConfig.blockAssistAndAutofill].
     * @param filterTouchesWhenObscured See [SecureScreenConfig.filterTouchesWhenObscured].
     */
    fun applyToViewTree(
        root: View,
        enabled: Boolean,
        blockAccessibilityTree: Boolean = true,
        scrubAccessibilityPayload: Boolean = true,
        markAccessibilityDataSensitive: Boolean = true,
        blockAssistAndAutofill: Boolean = true,
        filterTouchesWhenObscured: Boolean = true,
    ) {
        if (blockAccessibilityTree) {
            root.importantForAccessibility = if (enabled) {
                View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            } else {
                View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
            }
        }

        if (scrubAccessibilityPayload) {
            SecureAccessibilityScrubber.applyRecursively(root, enabled)
        }

        if (markAccessibilityDataSensitive && Build.VERSION.SDK_INT >= 34) {
            Api34Impl.setSensitive(root, enabled)
        }

        updateViewTree(root, enabled, blockAssistAndAutofill, filterTouchesWhenObscured)
    }

    /**
     * Recursively applies autofill blocking and overlay-touch filtering to [view]
     * and all its descendants.
     */
    private fun updateViewTree(
        view: View,
        enabled: Boolean,
        blockAssistAndAutofill: Boolean,
        filterTouchesWhenObscured: Boolean,
    ) {
        if (blockAssistAndAutofill) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                view.importantForAutofill = if (enabled) {
                    View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
                } else {
                    View.IMPORTANT_FOR_AUTOFILL_AUTO
                }
                if (enabled) {
                    view.setAutofillHints(*emptyArray<String>())
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setAssistBlockedIfAvailable(view, enabled)
            }
        }

        if (filterTouchesWhenObscured) {
            view.filterTouchesWhenObscured = enabled
        }

        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                updateViewTree(
                    view = view.getChildAt(index),
                    enabled = enabled,
                    blockAssistAndAutofill = blockAssistAndAutofill,
                    filterTouchesWhenObscured = filterTouchesWhenObscured,
                )
            }
        }
    }

    /**
     * Version-gated helper that calls [View.setAccessibilityDataSensitive], available
     * only on API 34 (Android 14) and above.
     *
     * Isolated in a nested object so the verifier does not attempt to resolve the symbol
     * on older API levels.
     */
    @RequiresApi(34)
    private object Api34Impl {
        fun setSensitive(view: View, enabled: Boolean) {
            view.setAccessibilityDataSensitive(
                if (enabled) {
                    View.ACCESSIBILITY_DATA_SENSITIVE_YES
                } else {
                    View.ACCESSIBILITY_DATA_SENSITIVE_AUTO
                },
            )
        }
    }

    /**
     * Attempts to call the hidden `View.setAssistBlocked(Boolean)` method via reflection.
     *
     * This is a non-public API present in AOSP since API 23. The call is wrapped in
     * [runCatching] so it silently no-ops on OEM builds that removed or renamed the method.
     */
    private fun setAssistBlockedIfAvailable(view: View, enabled: Boolean) {
        runCatching {
            View::class.java
                .getMethod("setAssistBlocked", Boolean::class.javaPrimitiveType)
                .invoke(view, enabled)
        }
    }
}
