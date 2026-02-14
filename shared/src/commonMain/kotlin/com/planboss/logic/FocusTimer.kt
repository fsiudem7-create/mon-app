package com.planboss.logic

import com.planboss.domain.FocusMode
import com.planboss.domain.FocusSession
import kotlinx.datetime.Clock

class FocusTimer(private val clock: Clock = Clock.System) {
    fun start(mode: FocusMode, taskId: String? = null, eventId: String? = null): FocusSession =
        FocusSession(
            id = "focus-${clock.now().toEpochMilliseconds()}",
            relatedTaskId = taskId,
            relatedEventId = eventId,
            startTime = clock.now(),
            mode = mode,
        )

    fun stop(session: FocusSession, note: String? = null): FocusSession {
        val end = clock.now()
        return session.copy(
            endTime = end,
            durationSeconds = (end.toEpochMilliseconds() - session.startTime.toEpochMilliseconds()) / 1000,
            notes = note,
        )
    }
}
