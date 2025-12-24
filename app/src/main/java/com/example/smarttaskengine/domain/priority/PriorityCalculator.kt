package com.example.smarttaskengine.domain.priority

import com.example.smarttaskengine.data.local.entity.TaskEntity
import kotlin.math.max

object PriorityCalculator {

    fun calculate(task: TaskEntity): Int {

        val now = System.currentTimeMillis()
        val timeDifference = task.deadline - now
        val hoursRemaining = timeDifference / (1000 * 60 * 60)

        val importanceScore = task.importance * 10

        val deadlineScore = when {
            hoursRemaining < 0 -> 50
            hoursRemaining <= 24 -> 30
            else -> max(1, (24 / hoursRemaining).toInt())
        }

        val effortScore = when {
            task.estimatedMinutes <= 30 -> 10
            task.estimatedMinutes <= 60 -> 5
            else -> 0
        }

        return importanceScore + deadlineScore + effortScore
    }
}
