package com.example.smarttaskengine.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttaskengine.data.local.entity.TaskEntity
import com.example.smarttaskengine.data.repository.TaskRepository
import com.example.smarttaskengine.domain.priority.PriorityCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> =
        repository.getTasks()
            .map { list ->
                list.sortedByDescending {
                    PriorityCalculator.calculate(it)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun addTask(
        title: String,
        importance: Int,
        estimatedMinutes: Int,
        deadline: Long
    ) {
        if (title.isBlank()) return
        if (importance !in 1..5) return

        val autoDeleteAt = System.currentTimeMillis() + (estimatedMinutes * 60 * 1000)

        val task = TaskEntity(
            title = title,
            importance = importance,
            estimatedMinutes = estimatedMinutes,
            deadline = deadline,
            autoDeleteAt = autoDeleteAt
        )

        viewModelScope.launch {
            repository.addTask(task)
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

}
