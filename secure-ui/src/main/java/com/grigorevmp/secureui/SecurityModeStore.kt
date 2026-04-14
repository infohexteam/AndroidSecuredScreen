package com.grigorevmp.secureui

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global, process-wide toggle that controls whether sensitive content is hidden.
 *
 * The current value is persisted in [android.content.SharedPreferences] under the file
 * `"secure_ui_mode"` and survives process restarts. It is exposed as a [StateFlow] so that
 * both coroutine-based (lifecycle scope) and Compose-based consumers can collect changes
 * reactively.
 *
 * This is a singleton obtained via [get]. The instance is created lazily with
 * double-checked locking and is scoped to the application context, so it is safe to call
 * from any `Context`.
 *
 * ```kotlin
 * val store = SecurityModeStore.get(context)
 * store.setContentHidden(true)   // enable protection
 * store.isContentHidden.collect { hidden -> /* react */ }
 * ```
 *
 * @see securityModeStore Extension shorthand on [Context].
 */
class SecurityModeStore private constructor(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val hiddenFlow = MutableStateFlow(prefs.getBoolean(KEY_HIDE_CONTENT, false))

    /**
     * Observable state of the secure-mode toggle.
     *
     * Emits `true` when sensitive content should be hidden, `false` otherwise.
     * Collect this flow in a `lifecycleScope` or via [androidx.compose.runtime.collectAsState]
     * to react to changes.
     */
    val isContentHidden: StateFlow<Boolean> = hiddenFlow.asStateFlow()

    /**
     * Sets the secure-mode flag and persists it to [android.content.SharedPreferences].
     *
     * If the new value equals the current one, the call is a no-op (no disk write,
     * no emission).
     *
     * @param hidden `true` to activate protection, `false` to deactivate.
     */
    fun setContentHidden(hidden: Boolean) {
        if (hiddenFlow.value == hidden) {
            return
        }
        prefs.edit()
            .putBoolean(KEY_HIDE_CONTENT, hidden)
            .apply()
        hiddenFlow.value = hidden
    }

    /**
     * Inverts the current secure-mode state and persists the result.
     *
     * Convenience wrapper around [setContentHidden] for toggle-style UI controls.
     */
    fun toggle() {
        setContentHidden(!hiddenFlow.value)
    }

    companion object {
        private const val PREFS_NAME = "secure_ui_mode"
        private const val KEY_HIDE_CONTENT = "hide_content"

        @Volatile
        private var instance: SecurityModeStore? = null

        /**
         * Returns the process-wide [SecurityModeStore] singleton.
         *
         * The instance is created lazily with double-checked locking and is scoped to the
         * application context derived from [context], so any `Context` (Activity, Service,
         * Application) is safe to pass in.
         *
         * @param context Any Android context; the application context is extracted internally.
         * @return The singleton [SecurityModeStore].
         */
        fun get(context: Context): SecurityModeStore {
            return instance ?: synchronized(this) {
                instance ?: SecurityModeStore(context).also { instance = it }
            }
        }
    }
}

/**
 * Shorthand for [SecurityModeStore.get] scoped to this [Context].
 *
 * ```kotlin
 * val store = context.securityModeStore()
 * ```
 */
fun Context.securityModeStore(): SecurityModeStore = SecurityModeStore.get(this)
