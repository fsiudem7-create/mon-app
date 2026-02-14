package com.planboss.logic

import com.planboss.domain.CalendarEvent
import com.planboss.domain.Link
import com.planboss.domain.LinkType
import com.planboss.domain.Task
import kotlin.math.max

class LinkingEngine {
    fun autoLink(tasks: List<Task>, events: List<CalendarEvent>): List<Link> {
        return tasks.mapNotNull { task ->
            val best = events.maxByOrNull { similarity(task.title, it.title) }
            val confidence = best?.let { similarity(task.title, it.title) } ?: 0.0
            if (best != null && confidence >= 0.55) {
                Link(task.id, best.id, confidence, LinkType.AUTO)
            } else null
        }
    }

    private fun similarity(a: String, b: String): Double {
        val left = a.lowercase().split(" ").toSet()
        val right = b.lowercase().split(" ").toSet()
        val inter = left.intersect(right).size
        val denom = max(left.size, right.size).coerceAtLeast(1)
        return inter.toDouble() / denom.toDouble()
    }
}
