package com.hexteam.screenprotect.attacker

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

internal fun ComponentActivity.configureAttackerEdgeToEdge() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.auto(
            LIGHT_SYSTEM_BAR_SCRIM,
            DARK_SYSTEM_BAR_SCRIM,
        ),
    )
}

private const val LIGHT_SYSTEM_BAR_SCRIM = 0xE6F4F0EA.toInt()
private const val DARK_SYSTEM_BAR_SCRIM = 0xCC111417.toInt()
