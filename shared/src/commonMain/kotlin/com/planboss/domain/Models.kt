package com.planboss.domain

import kotlinx.datetime.Instant

enum class TaskStatus { NOT_STARTED, IN_PROGRESS, DONE }
enum class Priority { LOW, MEDIUM, HIGH, CRITICAL }
enum class TaskSource { NOTION, GCAL, MANUAL }
enum class LinkType { MANUAL, AUTO }
enum class FocusMode { STOPWATCH, POMODORO }
enum class DisciplineMode { SOFT, STRICT }

data class Task(
    val id: String,
    val notionId: String? = null,
    val title: String,
    val status: TaskStatus,
    val dueDateTime: Instant? = null,
    val startDateTime: Instant? = null,
    val durationMinutes: Int? = null,
    val priority: Priority = Priority.MEDIUM,
    val source: TaskSource = TaskSource.MANUAL,
    val externalEventId: String? = null,
    val lastNotifiedAt: Instant? = null,
)

data class CalendarEvent(
    val id: String,
    val calendarId: String,
    val title: String,
    val startDateTime: Instant,
    val endDateTime: Instant,
    val location: String? = null,
    val description: String? = null,
    val isCompleted: Boolean = false,
)

data class Link(
    val taskId: String,
    val eventId: String,
    val linkConfidence: Double,
    val linkType: LinkType,
)

data class FocusSession(
    val id: String,
    val relatedTaskId: String? = null,
    val relatedEventId: String? = null,
    val startTime: Instant,
    val endTime: Instant? = null,
    val durationSeconds: Long = 0,
    val mode: FocusMode,
    val notes: String? = null,
)

data class SyncState(
    val provider: String,
    val lastSuccess: Instant? = null,
    val lastError: String? = null,
    val cursor: String? = null,
)

data class NotificationPolicy(
    val remindersBeforeMinutes: Set<Int> = setOf(30, 10, 0),
    val notifyIfNotStartedAfterMinutes: Int? = 15,
    val maxPerHour: Int = 3,
    val dailyReviewHour: Int = 21,
    val dailyReviewMinute: Int = 30,
    val disciplineMode: DisciplineMode = DisciplineMode.SOFT,
)

data class ComplianceScore(
    val tasksCompletedRatio: Double,
    val eventsCompletedRatio: Double,
    val focusRatio: Double,
) {
    val globalScore: Double = (tasksCompletedRatio * 0.5) + (eventsCompletedRatio * 0.2) + (focusRatio * 0.3)
}
