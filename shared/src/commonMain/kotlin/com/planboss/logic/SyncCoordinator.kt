package com.planboss.logic

import com.planboss.data.EventRepository
import com.planboss.data.GoogleCalendarClient
import com.planboss.data.NotionClient
import com.planboss.data.TaskRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class SyncCoordinator(
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository,
    private val notionClient: NotionClient,
    private val gcalClient: GoogleCalendarClient,
) {
    suspend fun sync(databaseId: String, selectedCalendars: List<String>) {
        val tasks = notionClient.queryTasksDatabase(databaseId)
        taskRepository.upsert(tasks)

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val timeMin = now.atStartOfDayIn(TimeZone.UTC)
        val timeMax = now.plus(DatePeriod(days = 30)).atStartOfDayIn(TimeZone.UTC)
        val events = selectedCalendars.flatMap { calendarId ->
            gcalClient.listEvents(calendarId, timeMin, timeMax)
        }
        eventRepository.upsert(events)
    }
}
