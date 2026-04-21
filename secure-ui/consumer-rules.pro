# secure-ui consumer ProGuard/R8 rules
# These rules are automatically applied to any app that depends on secure-ui.

# Keep the public API
-keep class com.hexteam.screenprotect.secureui.SecureActivity { *; }
-keep class com.hexteam.screenprotect.secureui.SecureUiPolicy { *; }
-keep class com.hexteam.screenprotect.secureui.SecurityModeStore { *; }
-keep class com.hexteam.screenprotect.secureui.SecureScreenConfig { *; }
-keep class com.hexteam.screenprotect.secureui.SecureFrameLayout { *; }

# Keep the @SecureScreen annotation (used via reflection in SecureActivity)
-keep @interface com.hexteam.screenprotect.secureui.SecureScreen

# Keep Kotlin extension functions (top-level)
-keep class com.hexteam.screenprotect.secureui.SecureViewExtensionsKt { *; }
-keep class com.hexteam.screenprotect.secureui.SecureComposeKt { *; }
-keep class com.hexteam.screenprotect.secureui.SecureComposeViewKt { *; }
-keep class com.hexteam.screenprotect.secureui.SecurityModeStoreKt { *; }

# The library uses reflection to call View.setAssistBlocked (hidden API)
-dontwarn android.view.View
