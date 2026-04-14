# secure-ui consumer ProGuard/R8 rules
# These rules are automatically applied to any app that depends on secure-ui.

# Keep the public API
-keep class com.grigorevmp.secureui.SecureActivity { *; }
-keep class com.grigorevmp.secureui.SecureUiPolicy { *; }
-keep class com.grigorevmp.secureui.SecurityModeStore { *; }
-keep class com.grigorevmp.secureui.SecureScreenConfig { *; }
-keep class com.grigorevmp.secureui.SecureFrameLayout { *; }

# Keep the @SecureScreen annotation (used via reflection in SecureActivity)
-keep @interface com.grigorevmp.secureui.SecureScreen

# Keep Kotlin extension functions (top-level)
-keep class com.grigorevmp.secureui.SecureViewExtensionsKt { *; }
-keep class com.grigorevmp.secureui.SecureComposeKt { *; }
-keep class com.grigorevmp.secureui.SecureComposeViewKt { *; }
-keep class com.grigorevmp.secureui.SecurityModeStoreKt { *; }

# The library uses reflection to call View.setAssistBlocked (hidden API)
-dontwarn android.view.View
