package com.planboss.ui

import com.planboss.data.InMemoryEventRepository
import com.planboss.data.InMemoryFocusRepository
import com.planboss.data.InMemoryTaskRepository
import com.planboss.domain.CalendarEvent
import com.planboss.domain.FocusMode
import com.planboss.domain.FocusSession
import com.planboss.domain.Task
import com.planboss.domain.TaskStatus
import com.planboss.logic.DisciplineEngine
import com.planboss.logic.FocusTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class PlanBossViewModel(
    private val taskRepository: InMemoryTaskRepository = InMemoryTaskRepository(),
    private val eventRepository: InMemoryEventRepository = InMemoryEventRepository(),
    private val focusRepository: InMemoryFocusRepository = InMemoryFocusRepository(),
    private val timer: FocusTimer = FocusTimer(),
    private val disciplineEngine: DisciplineEngine = DisciplineEngine(),
) {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    private val _focusSessions = MutableStateFlow<List<FocusSession>>(emptyList())

    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()
    val focusSessions: StateFlow<List<FocusSession>> = _focusSessions.asStateFlow()

    suspend fun seedDemoData() {
        val now = Clock.System.now()
        taskRepository.upsert(
            listOf(
                Task("t1", title = "Préparer sprint planning", status = TaskStatus.NOT_STARTED, dueDateTime = now, durationMinutes = 25),
                Task("t2", title = "Relire budget Q4", status = TaskStatus.IN_PROGRESS, dueDateTime = now, durationMinutes = 40),
            )
        )
        eventRepository.upsert(
            listOf(
                CalendarEvent("e1", "primary", "Sprint planning", now, now, "Room A", "Roadmap"),
                CalendarEvent("e2", "primary", "Deep work", now, now, null, null),
            )
        )
        _tasks.value = taskRepository.getTasks()
        _events.value = eventRepository.getEvents()
    }

    suspend fun markTaskDone(taskId: String) {
        taskRepository.markDone(taskId)
        _tasks.value = taskRepository.getTasks()
    }

    suspend fun markEventDone(eventId: String) {
        eventRepository.markCompleted(eventId)
        _events.value = eventRepository.getEvents()
    }

    suspend fun startFocus(taskId: String? = null): FocusSession {
        return timer.start(FocusMode.POMODORO, taskId = taskId)
    }

    suspend fun stopFocus(session: FocusSession, notes: String?): FocusSession {
        val completed = timer.stop(session, notes)
        focusRepository.add(completed)
        _focusSessions.value = focusRepository.all()
        return completed
    }

    fun disciplineMessage(): String {
        val score = disciplineEngine.computeScore(_tasks.value, _events.value, _focusSessions.value, plannedFocusMinutes = 60)
        val overdue = _tasks.value.count { it.status != TaskStatus.DONE }
        return disciplineEngine.coachingMessage(score, com.planboss.domain.DisciplineMode.SOFT, overdue)
    }
}
