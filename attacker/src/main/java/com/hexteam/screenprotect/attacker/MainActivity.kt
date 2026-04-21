package com.hexteam.screenprotect.attacker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hexteam.screenprotect.attacker.ui.AttackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        configureAttackerEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AttackerTheme {
                val logs by AttackLogStore.logs.collectAsState()
                AttackerScreen(
                    logs = logs,
                    openAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    clearLogs = AttackLogStore::clear,
                )
            }
        }
    }
}

@Composable
private fun AttackerScreen(
    logs: List<String>,
    openAccessibilitySettings: () -> Unit,
    clearLogs: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Accessibility attacker demo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "This module logs everything it can read from the victim app `com.hexteam.screenprotect.securedscreen`. When secure mode is off, real texts and field values should appear here. After secure mode is enabled, the logs should become empty or lose sensitive content.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = openAccessibilitySettings,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(text = "Open Accessibility")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = clearLogs,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(text = "Clear logs")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (logs.isEmpty()) {
                    item {
                        LogCard(
                            text = "No logs yet. Enable the service, open the victim app, and navigate through the XML and Compose screens.",
                        )
                    }
                } else {
                    items(logs) { log ->
                        LogCard(text = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogCard(text: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
