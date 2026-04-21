package com.hexteam.screenprotect.securedscreen

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.hexteam.screenprotect.secureui.SecureActivity
import com.hexteam.screenprotect.secureui.SecureFrameLayout
import com.hexteam.screenprotect.secureui.SecureScreen

@SecureScreen
class XmlSensitiveActivity : SecureActivity() {

    private lateinit var secureRoot: SecureFrameLayout
    private lateinit var contentScroll: View
    private lateinit var secretText: TextView
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var accountInput: EditText
    private lateinit var overlay: View
    private lateinit var statusText: TextView
    private var overlayDismissed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        configureDemoEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xml_sensitive)

        secureRoot = findViewById(R.id.secureRoot)
        contentScroll = findViewById(R.id.contentScroll)
        secretText = findViewById(R.id.secretText)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        accountInput = findViewById(R.id.accountInput)
        overlay = findViewById(R.id.overlay)
        statusText = findViewById(R.id.statusText)

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        findViewById<View>(R.id.overlayDismissButton).setOnClickListener {
            overlayDismissed = true
            updateOverlayVisibility()
        }
        secretText.text = getString(R.string.xml_secret_value)
        emailInput.setText(getString(R.string.xml_email_value))
        passwordInput.setText(getString(R.string.xml_password_value))
        accountInput.setText(getString(R.string.xml_account_value))
        installAccessibilityDemoWatchers()

        applyWindowInsets()
        applyXmlMode(isSecureModeEnabled())
    }

    override fun onSecureModeChanged(hidden: Boolean) {
        applyXmlMode(hidden)
    }

    private fun applyXmlMode(hidden: Boolean) {
        if (!hidden) {
            overlayDismissed = false
        }
        updateXmlAccessibilityExposure(hidden)
        updateOverlayVisibility()
        statusText.setText(
            if (hidden) {
                R.string.status_on
            } else {
                R.string.status_off
            },
        )
    }

    private fun updateOverlayVisibility() {
        overlay.visibility = if (isSecureModeEnabled() && !overlayDismissed) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun updateXmlAccessibilityExposure(hidden: Boolean) {
        val accessibilityMode = if (hidden) {
            View.IMPORTANT_FOR_ACCESSIBILITY_NO
        } else {
            View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }

        contentScroll.importantForAccessibility = accessibilityMode
        secretText.importantForAccessibility = accessibilityMode
        emailInput.importantForAccessibility = accessibilityMode
        passwordInput.importantForAccessibility = accessibilityMode
        accountInput.importantForAccessibility = accessibilityMode

        if (hidden) {
            contentScroll.contentDescription = null
            secretText.contentDescription = null
            emailInput.contentDescription = null
            passwordInput.contentDescription = null
            accountInput.contentDescription = null
            return
        }

        val emailValue = emailInput.text?.toString().orEmpty()
        val passwordValue = passwordInput.text?.toString().orEmpty()
        val accountValue = accountInput.text?.toString().orEmpty()
        val secretValue = secretText.text?.toString().orEmpty()

        contentScroll.contentDescription = buildString {
            append("XML demo accessibility summary. ")
            append("Secret: ")
            append(secretValue)
            append(". Email: ")
            append(emailValue)
            append(". Password: ")
            append(passwordValue)
            append(". Account: ")
            append(accountValue)
        }
        secretText.contentDescription = "Secret text. $secretValue"
        emailInput.contentDescription = "Email field. Current value $emailValue"
        passwordInput.contentDescription = "Password field. Current value $passwordValue"
        accountInput.contentDescription = "Account field. Current value $accountValue"

        contentScroll.post {
            if (!isSecureModeEnabled()) {
                contentScroll.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
            }
        }
    }

    private fun installAccessibilityDemoWatchers() {
        emailInput.doAfterTextChanged { refreshXmlAccessibilityDemoIfNeeded() }
        passwordInput.doAfterTextChanged { refreshXmlAccessibilityDemoIfNeeded() }
        accountInput.doAfterTextChanged { refreshXmlAccessibilityDemoIfNeeded() }
    }

    private fun refreshXmlAccessibilityDemoIfNeeded() {
        if (!isSecureModeEnabled()) {
            updateXmlAccessibilityExposure(hidden = false)
        }
    }

    private fun applyWindowInsets() {
        val contentBasePaddingTop = contentScroll.paddingTop
        val contentBasePaddingBottom = contentScroll.paddingBottom
        val overlayBasePaddingTop = overlay.paddingTop
        val overlayBasePaddingBottom = overlay.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(secureRoot) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            contentScroll.updatePadding(
                top = contentBasePaddingTop + systemBars.top,
                bottom = contentBasePaddingBottom + systemBars.bottom,
            )
            overlay.updatePadding(
                top = overlayBasePaddingTop + systemBars.top,
                bottom = overlayBasePaddingBottom + systemBars.bottom,
            )
            insets
        }
        ViewCompat.requestApplyInsets(secureRoot)
    }
}
