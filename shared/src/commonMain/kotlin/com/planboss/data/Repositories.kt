package com.planboss.data

import com.planboss.domain.CalendarEvent
import com.planboss.domain.FocusSession
import com.planboss.domain.Link
import com.planboss.domain.NotificationPolicy
import com.planboss.domain.Task
import com.planboss.domain.TaskStatus

interface TaskRepository {
    suspend fun getTasks(): List<Task>
    suspend fun upsert(tasks: List<Task>)
    suspend fun markDone(taskId: String)
}

interface EventRepository {
    suspend fun getEvents(): List<CalendarEvent>
    suspend fun upsert(events: List<CalendarEvent>)
    suspend fun markCompleted(eventId: String)
}

class InMemoryTaskRepository : TaskRepository {
    private val tasks = mutableMapOf<String, Task>()
    override suspend fun getTasks(): List<Task> = tasks.values.sortedBy { it.dueDateTime?.toString() }
    override suspend fun upsert(tasks: List<Task>) { tasks.forEach { this.tasks[it.id] = it } }
    override suspend fun markDone(taskId: String) {
        tasks[taskId]?.let { tasks[taskId] = it.copy(status = TaskStatus.DONE) }
    }
}

class InMemoryEventRepository : EventRepository {
    private val events = mutableMapOf<String, CalendarEvent>()
    override suspend fun getEvents(): List<CalendarEvent> = events.values.sortedBy { it.startDateTime.toString() }
    override suspend fun upsert(events: List<CalendarEvent>) { events.forEach { this.events[it.id] = it } }
    override suspend fun markCompleted(eventId: String) {
        events[eventId]?.let { events[eventId] = it.copy(isCompleted = true) }
    }
}

class InMemoryFocusRepository {
    private val sessions = mutableListOf<FocusSession>()
    suspend fun all(): List<FocusSession> = sessions.toList()
    suspend fun add(session: FocusSession) { sessions += session }
}

class SettingsRepository {
    private var policy = NotificationPolicy()
    suspend fun getNotificationPolicy(): NotificationPolicy = policy
    suspend fun setNotificationPolicy(value: NotificationPolicy) { policy = value }
}

class LinkRepository {
    private val links = mutableListOf<Link>()
    suspend fun getAll(): List<Link> = links.toList()
    suspend fun add(link: Link) { links += link }
}
