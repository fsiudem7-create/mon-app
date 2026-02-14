package com.planboss.logic

import com.planboss.domain.CalendarEvent
import com.planboss.domain.NotificationPolicy
import com.planboss.domain.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class NotificationRecord(val key: String, val timestamp: Instant)

class NotificationEngine(private val clock: Clock = Clock.System) {
    private val history = mutableListOf<NotificationRecord>()

    fun shouldNotify(key: String, policy: NotificationPolicy): Boolean {
        val now = clock.now()
        val oneHourAgo = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() - 3_600_000)
        history.removeAll { it.timestamp < oneHourAgo }
        if (history.count { it.timestamp >= oneHourAgo } >= policy.maxPerHour) return false
        if (history.any { it.key == key }) return false
        history += NotificationRecord(key, now)
        return true
    }

    fun buildDailyReview(tasks: List<Task>, events: List<CalendarEvent>): String {
        val doneTasks = tasks.count { it.status.name == "DONE" }
        val doneEvents = events.count { it.isCompleted }
        return "Daily review • Tasks done: $doneTasks/${tasks.size} • Events completed: $doneEvents/${events.size}"
    }

    fun reminderKeysForTask(task: Task, policy: NotificationPolicy): List<String> {
        val due = task.dueDateTime ?: return emptyList()
        val now = clock.now()
        return policy.remindersBeforeMinutes.mapNotNull { minutes ->
            val trigger = Instant.fromEpochMilliseconds(due.toEpochMilliseconds() - (minutes * 60_000L))
            if (trigger <= now) "task:${task.id}:$minutes" else null
        }
    }

    fun isDailyReviewTime(policy: NotificationPolicy): Boolean {
        val local = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return local.hour == policy.dailyReviewHour && local.minute >= policy.dailyReviewMinute
    }
}
