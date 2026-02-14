package com.planboss.data

import com.planboss.domain.CalendarEvent
import com.planboss.domain.Task
import com.planboss.domain.TaskStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GoogleCalendarClient(private val accessToken: String) {
    private val client = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun listCalendars(): List<GoogleCalendarDto> {
        val response = client.get("https://www.googleapis.com/calendar/v3/users/me/calendarList") {
            header("Authorization", "Bearer $accessToken")
        }.body<GoogleCalendarListResponse>()
        return response.items
    }

    suspend fun listEvents(calendarId: String, timeMin: Instant, timeMax: Instant): List<CalendarEvent> {
        val response = client.get("https://www.googleapis.com/calendar/v3/calendars/$calendarId/events") {
            header("Authorization", "Bearer $accessToken")
            url {
                parameters.append("singleEvents", "true")
                parameters.append("timeMin", timeMin.toString())
                parameters.append("timeMax", timeMax.toString())
            }
        }.body<GoogleEventsResponse>()
        return response.items.map {
            CalendarEvent(
                id = it.id,
                calendarId = calendarId,
                title = it.summary ?: "Untitled",
                startDateTime = Instant.parse(it.start.dateTime ?: "1970-01-01T00:00:00Z"),
                endDateTime = Instant.parse(it.end.dateTime ?: "1970-01-01T00:00:00Z"),
                location = it.location,
                description = it.description,
            )
        }
    }
}

class NotionClient(private val token: String) {
    private val client = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun queryTasksDatabase(databaseId: String): List<Task> {
        val response = client.get("https://api.notion.com/v1/databases/$databaseId/query") {
            header("Authorization", "Bearer $token")
            header("Notion-Version", "2022-06-28")
        }.body<NotionTasksResponse>()
        return response.results.map { page ->
            val properties = page.properties
            Task(
                id = page.id,
                notionId = page.id,
                title = properties.name?.title?.firstOrNull()?.plainText ?: "Untitled",
                status = when (properties.status?.select?.name) {
                    "Done" -> TaskStatus.DONE
                    "In progress" -> TaskStatus.IN_PROGRESS
                    else -> TaskStatus.NOT_STARTED
                },
                dueDateTime = properties.due?.date?.start?.let { Instant.parse(it) },
            )
        }
    }

    suspend fun updateTaskStatus(pageId: String, status: TaskStatus) {
        client.patch("https://api.notion.com/v1/pages/$pageId") {
            header("Authorization", "Bearer $token")
            header("Notion-Version", "2022-06-28")
            setBody(
                mapOf(
                    "properties" to mapOf(
                        "Status" to mapOf("select" to mapOf("name" to status.name.lowercase().replaceFirstChar { it.titlecase() }))
                    )
                )
            )
        }
    }
}

@Serializable data class GoogleCalendarListResponse(val items: List<GoogleCalendarDto>)
@Serializable data class GoogleCalendarDto(val id: String, val summary: String)
@Serializable data class GoogleEventsResponse(val items: List<GoogleEventDto>)
@Serializable data class GoogleEventDto(
    val id: String,
    val summary: String? = null,
    val location: String? = null,
    val description: String? = null,
    val start: GoogleDateTime,
    val end: GoogleDateTime,
)
@Serializable data class GoogleDateTime(@SerialName("dateTime") val dateTime: String? = null)

@Serializable data class NotionTasksResponse(val results: List<NotionPage>)
@Serializable data class NotionPage(val id: String, val properties: NotionProperties)
@Serializable data class NotionProperties(
    @SerialName("Name") val name: NotionTitleProperty? = null,
    @SerialName("Status") val status: NotionSelectProperty? = null,
    @SerialName("Due") val due: NotionDateProperty? = null,
)
@Serializable data class NotionTitleProperty(val title: List<NotionTitleText>)
@Serializable data class NotionTitleText(@SerialName("plain_text") val plainText: String)
@Serializable data class NotionSelectProperty(val select: NotionSelectValue? = null)
@Serializable data class NotionSelectValue(val name: String)
@Serializable data class NotionDateProperty(val date: NotionDateValue? = null)
@Serializable data class NotionDateValue(val start: String)
