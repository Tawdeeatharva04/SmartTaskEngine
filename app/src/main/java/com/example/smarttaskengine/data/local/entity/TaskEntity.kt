package com.example.smarttaskengine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String? = null,

    val importance: Int,        // 1 to 5
    val estimatedMinutes: Int,  // effort in minutes

    val deadline: Long,         // timestamp (ms)

    val createdAt: Long = System.currentTimeMillis(),

    val autoDeleteAt: Long

)
