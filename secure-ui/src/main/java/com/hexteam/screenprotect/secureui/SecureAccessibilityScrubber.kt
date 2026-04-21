package com.hexteam.screenprotect.secureui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Collections
import java.util.WeakHashMap

/**
 * Replaces the [View.AccessibilityDelegate] on every view in a subtree with a
 * scrubbing proxy that nullifies all sensitive accessibility data.
 *
 * Original delegates are stored in a [WeakHashMap] keyed on the view instance, so
 * they are automatically garbage-collected when the view is destroyed. When protection
 * is disabled, the original delegate is restored.
 *
 * This object is `internal` — callers should go through [SecureUiPolicy.applyToViewTree]
 * or the higher-level APIs ([SecureActivity], [ApplySecureComposeHostPolicy], etc.).
 */
internal object SecureAccessibilityScrubber {

    /**
     * Thread-safe map from view to its original [View.AccessibilityDelegate].
     *
     * Uses [WeakHashMap] so entries are evicted when the view is GC'd, preventing
     * memory leaks in long-lived view hierarchies.
     */
    private val originalDelegates = Collections.synchronizedMap(
        WeakHashMap<View, View.AccessibilityDelegate?>(),
    )

    /**
     * Recursively installs (or removes) scrubbing delegates on [root] and every
     * descendant in the view tree.
     *
     * When [enabled] is `true`, each view's current delegate is saved in
     * [originalDelegates] and replaced with a [ScrubbingDelegate]. When `false`,
     * the original delegate is restored.
     *
     * @param root    The root of the subtree to process.
     * @param enabled `true` to install scrubbing, `false` to restore originals.
     */
    fun applyRecursively(
        root: View,
        enabled: Boolean,
    ) {
        updateDelegate(root, enabled)
        if (root is ViewGroup) {
            for (index in 0 until root.childCount) {
                applyRecursively(root.getChildAt(index), enabled)
            }
        }
    }

    /**
     * Installs or removes the scrubbing delegate on a single [view].
     *
     * If [enabled] and the view does not already have a scrubbing delegate, the current
     * delegate is saved and replaced. If not [enabled] and a saved delegate exists, it
     * is restored.
     */
    private fun updateDelegate(
        view: View,
        enabled: Boolean,
    ) {
        if (enabled) {
            if (!originalDelegates.containsKey(view)) {
                val originalDelegate = view.accessibilityDelegate
                originalDelegates[view] = originalDelegate
                view.accessibilityDelegate = ScrubbingDelegate()
            }
            return
        }

        if (originalDelegates.containsKey(view)) {
            view.accessibilityDelegate = originalDelegates.remove(view)
        }
    }

    /**
     * An [View.AccessibilityDelegate] that silences all accessibility output.
     *
     * Every callback is overridden to either no-op or strip all meaningful data from
     * the [AccessibilityEvent] and [AccessibilityNodeInfo] before they reach
     * accessibility services.
     */
    private class ScrubbingDelegate : View.AccessibilityDelegate() {

        /** Swallows the event entirely — no event is sent to the framework. */
        override fun sendAccessibilityEvent(host: View, eventType: Int) = Unit

        /** Strips text and content description before the unchecked send. */
        override fun sendAccessibilityEventUnchecked(
            host: View,
            event: AccessibilityEvent,
        ) {
            sanitizeEvent(event)
        }

        /** Sanitizes the event and returns `true` to signal "handled, stop propagation". */
        override fun dispatchPopulateAccessibilityEvent(
            host: View,
            event: AccessibilityEvent,
        ): Boolean {
            sanitizeEvent(event)
            return true
        }

        /** Strips text and content description during the populate phase. */
        override fun onPopulateAccessibilityEvent(
            host: View,
            event: AccessibilityEvent,
        ) {
            sanitizeEvent(event)
        }

        /** Sanitizes the event and resets the class name to plain [View]. */
        override fun onInitializeAccessibilityEvent(
            host: View,
            event: AccessibilityEvent,
        ) {
            sanitizeEvent(event)
            event.className = View::class.java.name
        }

        /**
         * Nullifies every meaningful field on the [AccessibilityNodeInfo].
         *
         * This covers text, content description, class name, hints, tooltips, actions,
         * children, extras, and version-gated fields up to API 36.
         */
        override fun onInitializeAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfo,
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            sanitizeNode(host, info)
        }

        /**
         * Sanitizes the child event and returns `false` to block the request from
         * propagating to the parent.
         */
        override fun onRequestSendAccessibilityEvent(
            host: ViewGroup,
            child: View,
            event: AccessibilityEvent,
        ): Boolean {
            sanitizeEvent(event)
            return false
        }

        /** Blocks all accessibility actions. */
        override fun performAccessibilityAction(
            host: View,
            action: Int,
            args: Bundle?,
        ): Boolean = false

        /** Returns `null` — no virtual accessibility node tree is exposed. */
        override fun getAccessibilityNodeProvider(host: View) = null

        /** Clears [AccessibilityEvent.text] and nullifies [AccessibilityEvent.contentDescription]. */
        private fun sanitizeEvent(event: AccessibilityEvent) {
            event.text.clear()
            event.contentDescription = null
        }

        /**
         * Scrubs every sensitive field from [info], progressing through API-level-gated
         * blocks.
         *
         * Fields cleared: text, contentDescription, className, clickable/focusable/scrollable
         * flags, viewIdResourceName, error, collectionInfo, rangeInfo, hintText (API 26+),
         * tooltipText/paneTitle (API 28+), textEntryKey (API 29+), stateDescription (API 30+),
         * containerTitle (API 36+), uniqueId (API 34+), all actions, all children, and extras.
         */
        private fun sanitizeNode(
            host: View,
            info: AccessibilityNodeInfo,
        ) {
            info.text = null
            info.contentDescription = null
            info.className = View::class.java.name
            info.packageName = host.context.packageName
            info.isClickable = false
            info.isLongClickable = false
            info.isFocusable = false
            info.isScrollable = false
            info.isEnabled = false
            info.isVisibleToUser = false
            info.setViewIdResourceName(null)
            info.setError(null)
            info.setCollectionInfo(null)
            info.setCollectionItemInfo(null)
            info.setRangeInfo(null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                info.setHintText(null)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.isScreenReaderFocusable = false
                info.isShowingHintText = false
                info.setTooltipText(null)
                info.setPaneTitle(null)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                info.setTextEntryKey(false)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                info.setStateDescription(null)
            }
            if (Build.VERSION.SDK_INT >= 36) {
                info.setContainerTitle(null)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                info.uniqueId = null
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                info.actionList.toList().forEach(info::removeAction)
            }
            if (host is ViewGroup) {
                for (index in 0 until host.childCount) {
                    info.removeChild(host.getChildAt(index))
                }
            }
            info.extras.clear()
        }
    }
}
