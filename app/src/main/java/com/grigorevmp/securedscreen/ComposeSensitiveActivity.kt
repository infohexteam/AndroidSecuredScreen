package com.grigorevmp.securedscreen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grigorevmp.secureui.ApplySecureComposeHostPolicy
import com.grigorevmp.secureui.SecureActivity
import com.grigorevmp.secureui.SecureScreen
import com.grigorevmp.secureui.rememberSecureContentHidden
import com.grigorevmp.secureui.secureSemantics
import com.grigorevmp.securedscreen.ui.SecurityDemoTheme

@SecureScreen
class ComposeSensitiveActivity : SecureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        configureDemoEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SecurityDemoTheme {
                ApplySecureComposeHostPolicy()
                val hidden by rememberSecureContentHidden()
                ComposeSensitiveScreen(
                    hidden = hidden,
                    onBack = ::finish,
                )
            }
        }
    }
}

@Composable
private fun ComposeSensitiveScreen(
    hidden: Boolean,
    onBack: () -> Unit,
) {
    var login by rememberSaveable { mutableStateOf("ceo@secured.demo") }
    var password by rememberSaveable { mutableStateOf("P@55w0rd!compose") }
    var cardNumber by rememberSaveable { mutableStateOf("4242 4242 4242 4242") }
    var overlayDismissed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(hidden) {
        if (!hidden) {
            overlayDismissed = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .secureSemantics(hidden),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(onClick = onBack, shape = RoundedCornerShape(20.dp)) {
                    Text(text = "Back")
                }

                Text(
                    text = "Compose screen with sensitive content",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "When secure mode is off, the accessibility service can read this text and the field values. When it is on, the activity blocks the entire tree with a best-effort policy.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Compose secret",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = "Confirmation code 517 204 and backup phrase orbit stone maple.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = login,
                    onValueChange = { login = it },
                    label = { Text(text = "Client login") },
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(text = "Password") },
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text(text = "Card number") },
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (hidden && !overlayDismissed) {
                SecureOverlay(
                    onHideOverlay = { overlayDismissed = true },
                )
            }
        }
    }
}

@Composable
private fun SecureOverlay(
    onHideOverlay: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Secure mode enabled",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "The accessibility tree is hidden. In the attacker app, logs should stop containing sensitive texts and field values from this screen.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onHideOverlay,
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(text = stringResource(R.string.overlay_hide_inline))
                }
            }
        }
    }
}
