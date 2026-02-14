package com.planboss.settings

import com.planboss.domain.DisciplineMode

data class AppSettings(
    val reminder30m: Boolean = true,
    val reminder10m: Boolean = true,
    val reminder0m: Boolean = true,
    val reminderNotStarted15m: Boolean = true,
    val dailyReviewTime: String = "21:30",
    val disciplineMode: DisciplineMode = DisciplineMode.SOFT,
    val syncIntervalMinutes: Int = 30,
)
