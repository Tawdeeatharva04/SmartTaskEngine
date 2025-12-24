package com.example.smarttaskengine.data.local.database

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters


class TaskCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = TaskDatabase.getInstance(applicationContext)
        val now = System.currentTimeMillis()

        db.taskDao().deleteExpiredTasks(now)

        return Result.success()
    }
}
