package com.grigorevmp.securedscreen

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

internal fun ComponentActivity.configureDemoEdgeToEdge() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.auto(
            LIGHT_SYSTEM_BAR_SCRIM,
            DARK_SYSTEM_BAR_SCRIM,
        ),
    )
}

private const val LIGHT_SYSTEM_BAR_SCRIM = 0xE6F6F1E8.toInt()
private const val DARK_SYSTEM_BAR_SCRIM = 0xCC161A20.toInt()
