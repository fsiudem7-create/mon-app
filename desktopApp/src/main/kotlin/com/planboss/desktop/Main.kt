package com.planboss.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.planboss.ui.PlanBossApp

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "PlanBoss") {
        PlanBossApp()
    }
}
