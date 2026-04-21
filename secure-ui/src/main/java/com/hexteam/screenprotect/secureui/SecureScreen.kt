package com.hexteam.screenprotect.secureui

/**
 * Marks an Activity subclass for automatic secure-mode policy application.
 *
 * When placed on a class that extends [SecureActivity], the annotation is read at
 * runtime via reflection. Each flag maps to a specific protection layer that can be
 * toggled independently. All flags default to `true` (maximum protection).
 *
 * Example:
 * ```kotlin
 * @SecureScreen(blockScreenshots = true, scrubAccessibilityPayload = true)
 * class PaymentActivity : SecureActivity()
 * ```
 *
 * @property blockAccessibilityTree Sets [android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS]
 *   on the root view, hiding the entire subtree from accessibility services.
 * @property scrubAccessibilityPayload Replaces every view's [android.view.View.AccessibilityDelegate]
 *   with a scrubbing proxy that nullifies text, content descriptions, actions, and node metadata
 *   in [android.view.accessibility.AccessibilityEvent] and [android.view.accessibility.AccessibilityNodeInfo].
 * @property markAccessibilityDataSensitive On API 34+ calls
 *   [android.view.View.setAccessibilityDataSensitive] with
 *   [android.view.View.ACCESSIBILITY_DATA_SENSITIVE_YES].
 * @property blockAssistAndAutofill Disables autofill via
 *   [android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS] and calls the hidden
 *   `View.setAssistBlocked(true)` via reflection on API 23+.
 * @property blockScreenshots Adds [android.view.WindowManager.LayoutParams.FLAG_SECURE] to the
 *   host window, preventing screenshots and screen recording.
 * @property filterTouchesWhenObscured Enables [android.view.View.setFilterTouchesWhenObscured]
 *   to reject touch events when another app's window overlays this view.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SecureScreen(
    val blockAccessibilityTree: Boolean = true,
    val scrubAccessibilityPayload: Boolean = true,
    val markAccessibilityDataSensitive: Boolean = true,
    val blockAssistAndAutofill: Boolean = true,
    val blockScreenshots: Boolean = true,
    val filterTouchesWhenObscured: Boolean = true,
)

/**
 * Runtime representation of the six protection flags from [SecureScreen].
 *
 * Use this data class when you need to configure protection programmatically
 * (e.g. in Compose or View extensions) without relying on the annotation.
 *
 * All flags default to `true` (maximum protection).
 *
 * @property blockAccessibilityTree See [SecureScreen.blockAccessibilityTree].
 * @property scrubAccessibilityPayload See [SecureScreen.scrubAccessibilityPayload].
 * @property markAccessibilityDataSensitive See [SecureScreen.markAccessibilityDataSensitive].
 * @property blockAssistAndAutofill See [SecureScreen.blockAssistAndAutofill].
 * @property blockScreenshots See [SecureScreen.blockScreenshots].
 * @property filterTouchesWhenObscured See [SecureScreen.filterTouchesWhenObscured].
 */
data class SecureScreenConfig(
    val blockAccessibilityTree: Boolean = true,
    val scrubAccessibilityPayload: Boolean = true,
    val markAccessibilityDataSensitive: Boolean = true,
    val blockAssistAndAutofill: Boolean = true,
    val blockScreenshots: Boolean = true,
    val filterTouchesWhenObscured: Boolean = true,
)

/**
 * Converts a [SecureScreen] annotation instance into a [SecureScreenConfig] data class,
 * copying every flag value as-is.
 */
internal fun SecureScreen.toConfig(): SecureScreenConfig = SecureScreenConfig(
    blockAccessibilityTree = blockAccessibilityTree,
    scrubAccessibilityPayload = scrubAccessibilityPayload,
    markAccessibilityDataSensitive = markAccessibilityDataSensitive,
    blockAssistAndAutofill = blockAssistAndAutofill,
    blockScreenshots = blockScreenshots,
    filterTouchesWhenObscured = filterTouchesWhenObscured,
)
