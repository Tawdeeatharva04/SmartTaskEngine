package com.example.smarttaskengine.ui.tasklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smarttaskengine.R
import com.example.smarttaskengine.data.local.entity.TaskEntity
import com.example.smarttaskengine.domain.priority.PriorityCalculator

class TaskAdapter : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val items = mutableListOf<TaskEntity>()

    fun submitList(newItems: List<TaskEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getTaskAt(position: Int): TaskEntity {
        return items[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val indicator: View = itemView.findViewById(R.id.priorityIndicator)

        fun bind(task: TaskEntity) {

            val now = System.currentTimeMillis()
            val minutesLeft = (task.autoDeleteAt - now) / (1000 * 60)

            tvTitle.text = task.title
            tvPriority.text = "Time left: $minutesLeft min"

            val colorRes = when {
                minutesLeft <= 10 -> R.color.priority_high      // 🔴 urgent
                minutesLeft in 11..30 -> R.color.priority_medium // 🟠 medium
                minutesLeft > 30 && task.importance >= 4 -> R.color.priority_low // 🟢 good
                task.importance <= 3 -> R.color.priority_medium // 🟠 low importance
                else -> R.color.priority_high
            }

            indicator.setBackgroundColor(
                ContextCompat.getColor(itemView.context, colorRes)
            )

        }
    }
}
