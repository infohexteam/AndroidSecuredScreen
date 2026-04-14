package com.grigorevmp.securedscreen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.grigorevmp.secureui.rememberSecurityModeStore
import com.grigorevmp.securedscreen.ui.SecurityDemoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        configureDemoEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SecurityDemoTheme {
                val securityStore = rememberSecurityModeStore()
                val hidden by securityStore.isContentHidden.collectAsState()

                HomeScreen(
                    hidden = hidden,
                    onToggleHidden = securityStore::setContentHidden,
                    openCompose = { startActivity(Intent(this, ComposeSensitiveActivity::class.java)) },
                    openXml = { startActivity(Intent(this, XmlSensitiveActivity::class.java)) },
                    openAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    openAttackerApp = {
                        val launchIntent = packageManager.getLaunchIntentForPackage("com.grigorevmp.attacker")
                        if (launchIntent == null) {
                            Toast.makeText(this, getString(R.string.attacker_missing), Toast.LENGTH_LONG).show()
                        } else {
                            try {
                                startActivity(launchIntent)
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(this, getString(R.string.attacker_missing), Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    hidden: Boolean,
    onToggleHidden: (Boolean) -> Unit,
    openCompose: () -> Unit,
    openXml: () -> Unit,
    openAccessibilitySettings: () -> Unit,
    openAttackerApp: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Secure Screen Demo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "This demo shows that the accessibility service can read sensitive texts and input fields by default. After you enable the global switch, the library moves screens into best-effort secure mode.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.switch_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (hidden) {
                                stringResource(R.string.switch_summary_on)
                            } else {
                                stringResource(R.string.switch_summary_off)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Switch(
                        checked = hidden,
                        onCheckedChange = onToggleHidden,
                    )
                }
            }

            GuidanceCard(
                title = "How to test",
                body = "1. Install and enable the attacker app as an Accessibility Service. 2. Open the XML or Compose screen. 3. Check the logs in the attacker app before and after enabling the switch.",
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.open_compose),
                    onClick = openCompose,
                )
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.open_xml),
                    onClick = openXml,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.open_accessibility_settings),
                    onClick = openAccessibilitySettings,
                )
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.open_attacker_app),
                    onClick = openAttackerApp,
                )
            }
        }
    }
}

@Composable
private fun GuidanceCard(
    title: String,
    body: String,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
    ) {
        Text(text = text)
    }
}
