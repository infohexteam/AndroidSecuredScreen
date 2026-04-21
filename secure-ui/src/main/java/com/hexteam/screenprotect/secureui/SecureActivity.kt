package com.hexteam.screenprotect.secureui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Base [AppCompatActivity] that automatically applies the [SecureUiPolicy] to its window
 * and content view tree whenever [SecurityModeStore.isContentHidden] changes.
 *
 * Subclasses must be annotated with [@SecureScreen][SecureScreen] to opt in. The annotation
 * is read once at startup via `javaClass.getAnnotation(...)` and converted to a
 * [SecureScreenConfig]. If the annotation is absent, no policy is applied.
 *
 * The policy is re-applied on three occasions:
 * 1. Every time [SecurityModeStore.isContentHidden] emits a new value
 *    (collected in [Lifecycle.State.STARTED]).
 * 2. In [onPostCreate] — catches the initial layout pass.
 * 3. In [onContentChanged] — catches calls to [setContentView].
 *
 * Example:
 * ```kotlin
 * @SecureScreen
 * class PaymentActivity : SecureActivity() {
 *     override fun onSecureModeChanged(hidden: Boolean) {
 *         // optional: react to the toggle in your own UI
 *     }
 * }
 * ```
 *
 * @see SecureScreen Annotation that configures which protection layers are active.
 * @see SecureUiPolicy The policy engine that does the actual work.
 * @see SecurityModeStore The global toggle that drives the state.
 */
abstract class SecureActivity : AppCompatActivity() {

    /**
     * Lazily-initialized reference to the process-wide [SecurityModeStore].
     *
     * Subclasses can use this to read or write the current secure-mode state
     * (e.g. to wire up a `Switch` or `Toggle`).
     */
    protected val securityModeStore: SecurityModeStore by lazy(LazyThreadSafetyMode.NONE) {
        SecurityModeStore.get(this)
    }

    /**
     * Cached [SecureScreenConfig] derived from the [@SecureScreen][SecureScreen] annotation
     * on the concrete subclass, or `null` if the annotation is missing.
     */
    private val secureConfig: SecureScreenConfig? by lazy(LazyThreadSafetyMode.NONE) {
        javaClass.getAnnotation(SecureScreen::class.java)?.toConfig()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = secureConfig ?: return
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                securityModeStore.isContentHidden.collect { hidden ->
                    applySecureState(hidden, config)
                    onSecureModeChanged(hidden)
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        applyCurrentSecureState()
    }

    override fun onContentChanged() {
        super.onContentChanged()
        applyCurrentSecureState()
    }

    /**
     * Called every time the secure-mode state changes while the activity is at least
     * [Lifecycle.State.STARTED].
     *
     * Override this to update your own UI in response to the toggle (e.g. show a
     * placeholder overlay, update a status label, etc.). The default implementation
     * is a no-op.
     *
     * @param hidden `true` when sensitive content should be hidden, `false` otherwise.
     */
    protected open fun onSecureModeChanged(hidden: Boolean) = Unit

    /**
     * Returns the current value of [SecurityModeStore.isContentHidden] synchronously.
     *
     * Useful in lifecycle callbacks where you need a one-shot read rather than a
     * reactive stream.
     */
    protected fun isSecureModeEnabled(): Boolean = securityModeStore.isContentHidden.value

    private fun applyCurrentSecureState() {
        val config = secureConfig ?: return
        applySecureState(isSecureModeEnabled(), config)
    }

    private fun applySecureState(hidden: Boolean, config: SecureScreenConfig) {
        SecureUiPolicy.apply(
            window = window,
            root = findViewById<View>(android.R.id.content),
            enabled = hidden,
            config = config,
        )
    }
}
