package com.planboss.logic

import com.planboss.domain.CalendarEvent
import com.planboss.domain.NotificationPolicy
import com.planboss.domain.Task
import com.planboss.domain.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class MutableClock(private var nowInstant: Instant) : Clock {
    override fun now(): Instant = nowInstant
    fun advanceMillis(delta: Long) { nowInstant = Instant.fromEpochMilliseconds(nowInstant.toEpochMilliseconds() + delta) }
}

class EnginesTest {
    @Test
    fun linkingEngine_links_similar_titles() {
        val engine = LinkingEngine()
        val links = engine.autoLink(
            listOf(Task("1", title = "Sprint planning équipe", status = TaskStatus.NOT_STARTED)),
            listOf(CalendarEvent("e1", "c1", "Sprint planning", Instant.DISTANT_PAST, Instant.DISTANT_PAST))
        )
        assertEquals(1, links.size)
        assertTrue(links.first().linkConfidence >= 0.55)
    }

    @Test
    fun notificationEngine_applies_rate_limit_and_dedup() {
        val clock = MutableClock(Instant.parse("2026-01-01T10:00:00Z"))
        val engine = NotificationEngine(clock)
        val policy = NotificationPolicy(maxPerHour = 2)

        assertTrue(engine.shouldNotify("a", policy))
        assertFalse(engine.shouldNotify("a", policy))
        assertTrue(engine.shouldNotify("b", policy))
        assertFalse(engine.shouldNotify("c", policy))

        clock.advanceMillis(3_600_001)
        assertTrue(engine.shouldNotify("c", policy))
    }

    @Test
    fun disciplineEngine_computes_expected_score() {
        val engine = DisciplineEngine()
        val score = engine.computeScore(
            tasks = listOf(
                Task("1", title = "A", status = TaskStatus.DONE),
                Task("2", title = "B", status = TaskStatus.NOT_STARTED)
            ),
            events = listOf(
                CalendarEvent("e1", "c", "E", Instant.DISTANT_PAST, Instant.DISTANT_PAST, isCompleted = true)
            ),
            sessions = emptyList(),
            plannedFocusMinutes = 60
        )
        assertEquals(0.45, score.globalScore, absoluteTolerance = 0.01)
    }
}
