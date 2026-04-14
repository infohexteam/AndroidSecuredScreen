package com.grigorevmp.secureui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * A [FrameLayout] subclass with no custom logic, provided as a semantic convenience for
 * XML layouts.
 *
 * Using `SecureFrameLayout` in your layout XML makes it clear that the subtree is intended
 * to be protected by the secure-ui policy. You can then call [applySecurePolicy] or
 * [bindSecurityMode] on this view to activate protection.
 *
 * ```xml
 * <com.grigorevmp.secureui.SecureFrameLayout
 *     android:id="@+id/secureRoot"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent">
 *     <!-- sensitive content here -->
 * </com.grigorevmp.secureui.SecureFrameLayout>
 * ```
 */
class SecureFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr)
