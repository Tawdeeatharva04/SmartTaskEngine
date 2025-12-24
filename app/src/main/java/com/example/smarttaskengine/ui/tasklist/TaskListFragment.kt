package com.example.smarttaskengine.ui.tasklist

import android.app.DatePickerDialog
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smarttaskengine.R
import com.example.smarttaskengine.data.local.database.TaskDatabase
import com.example.smarttaskengine.data.local.entity.TaskEntity
import com.example.smarttaskengine.data.repository.TaskRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.work.*
import com.example.smarttaskengine.data.local.database.TaskCleanupWorker
import java.util.concurrent.TimeUnit


class TaskListFragment : Fragment(R.layout.fragment_task_list) {

    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter

    private var recentlyDeletedTask: TaskEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Database & ViewModel
        val database = TaskDatabase.getInstance(requireContext())
        val repository = TaskRepository(database.taskDao())
        viewModel = ViewModelProvider(
            this,
            TaskViewModelFactory(repository)
        )[TaskViewModel::class.java]

        // RecyclerView
        adapter = TaskAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.taskRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 🔴 ATTACH ITEM TOUCH HELPER (MISSING BEFORE)
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        val cleanupWork = PeriodicWorkRequestBuilder<TaskCleanupWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                "task_cleanup",
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupWork
            )

        lifecycleScope.launch {
            val db = TaskDatabase.getInstance(requireContext())
            db.taskDao().deleteExpiredTasks(System.currentTimeMillis())
        }



        // Inputs
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etMinutes = view.findViewById<EditText>(R.id.etMinutes)
        val seekImportance = view.findViewById<SeekBar>(R.id.seekImportance)
        val tvImportanceValue = view.findViewById<TextView>(R.id.tvImportanceValue)
        val tvDeadline = view.findViewById<TextView>(R.id.tvDeadline)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)

        var importanceValue = 3
        var selectedDeadline = System.currentTimeMillis()
        tvImportanceValue.text = importanceValue.toString()

        seekImportance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                importanceValue = progress + 1
                tvImportanceValue.text = importanceValue.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        tvDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day, 23, 59)
                    selectedDeadline = calendar.timeInMillis
                    tvDeadline.text = "$day/${month + 1}/$year"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnAdd.setOnClickListener {
            viewModel.addTask(
                title = etTitle.text.toString(),
                importance = importanceValue,
                estimatedMinutes = etMinutes.text.toString().toIntOrNull() ?: 30,
                deadline = selectedDeadline
            )
            etTitle.text.clear()
        }

        // Observe data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasks.collect {
                adapter.submitList(it)
            }
        }
    }

    // ✅ CLEAN SWIPE CALLBACK
    private val swipeCallback = object : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val task = adapter.getTaskAt(viewHolder.adapterPosition)
            recentlyDeletedTask = task
            viewModel.deleteTask(task.id)
            showUndoSnackbar()
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            // ✅ SUPER FIRST (CRITICAL)
            super.onChildDraw(
                c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
            )

            val itemView = viewHolder.itemView
            val background = ColorDrawable(
                ContextCompat.getColor(requireContext(), R.color.swipe_delete_bg)
            )
            val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)!!
            val iconMargin = (itemView.height - icon.intrinsicHeight) / 2

            if (dX > 0) {
                background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                icon.setBounds(
                    itemView.left + iconMargin,
                    itemView.top + iconMargin,
                    itemView.left + iconMargin + icon.intrinsicWidth,
                    itemView.bottom - iconMargin
                )
            } else {
                background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                icon.setBounds(
                    itemView.right - iconMargin - icon.intrinsicWidth,
                    itemView.top + iconMargin,
                    itemView.right - iconMargin,
                    itemView.bottom - iconMargin
                )
            }

            background.draw(c)
            icon.draw(c)
        }
    }

    private fun showUndoSnackbar() {
        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                recentlyDeletedTask?.let {
                    viewModel.addTask(
                        it.title,
                        it.importance,
                        it.estimatedMinutes,
                        it.deadline
                    )
                }
            }
            .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
            .show()
    }
}
