package com.planboss.logic

import com.planboss.domain.CalendarEvent
import com.planboss.domain.ComplianceScore
import com.planboss.domain.DisciplineMode
import com.planboss.domain.FocusSession
import com.planboss.domain.Task
import com.planboss.domain.TaskStatus

class DisciplineEngine {
    fun computeScore(tasks: List<Task>, events: List<CalendarEvent>, sessions: List<FocusSession>, plannedFocusMinutes: Int): ComplianceScore {
        val taskRatio = if (tasks.isEmpty()) 1.0 else tasks.count { it.status == TaskStatus.DONE }.toDouble() / tasks.size
        val eventRatio = if (events.isEmpty()) 1.0 else events.count { it.isCompleted }.toDouble() / events.size
        val focusedMinutes = sessions.sumOf { (it.durationSeconds / 60.0) }
        val focusRatio = if (plannedFocusMinutes == 0) 1.0 else (focusedMinutes / plannedFocusMinutes).coerceAtMost(1.0)
        return ComplianceScore(taskRatio, eventRatio, focusRatio)
    }

    fun coachingMessage(score: ComplianceScore, mode: DisciplineMode, overdueCount: Int): String {
        if (score.globalScore >= 0.7) return "Excellent suivi aujourd'hui. Continue comme ça."
        return if (mode == DisciplineMode.STRICT) {
            "Tu dévies du plan. $overdueCount tâches en retard. Choisis 1 tâche maintenant."
        } else {
            "On rattrape ça : choisis une tâche courte de 10 min."
        }
    }

    fun recoveryPlan(tasks: List<Task>): List<String> = tasks.sortedBy { it.durationMinutes ?: 20 }
        .take(3)
        .mapIndexed { index, task -> "${index + 1}. ${task.title} (${task.durationMinutes ?: 20} min)" }
}
