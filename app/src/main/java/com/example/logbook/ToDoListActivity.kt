package com.example.logbook

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ToDoListActivity : AppCompatActivity() {
    private lateinit var editTextTask: EditText
    private lateinit var buttonAdd: Button
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private lateinit var dbHelper: TaskDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_do_list)

        dbHelper = TaskDatabaseHelper(this)

        editTextTask = findViewById(R.id.editTextTask)
        buttonAdd = findViewById(R.id.buttonAdd)
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)

        taskAdapter = TaskAdapter(tasks)
        recyclerViewTasks.adapter = taskAdapter
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        recyclerViewTasks.addItemDecoration(TaskItemDecoration(4)) // Reduce spacing to 4dp

        buttonAdd.setOnClickListener {
            addTask()
        }

        editTextTask.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                addTask()
                return@setOnKeyListener true
            }
            false
        }

        // Load tasks from the database
        tasks.addAll(dbHelper.getAllTasks())
        taskAdapter.notifyDataSetChanged()

        setupDateAndTime()
        updateCompletionPercentage()
    }

    private fun addTask() {
        val taskText = editTextTask.text.toString().trim()
        if (taskText.isNotEmpty()) {
            val task = Task(id = 0, value = taskText) // Set id to 0, it will be updated after insertion
            val insertedId = dbHelper.addTask(task)
            task.id = insertedId.toInt()
            tasks.add(task)
            taskAdapter.notifyItemInserted(tasks.size - 1)
            editTextTask.text.clear()
            updateCompletionPercentage()
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { view ->
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setupDateAndTime() {
        val textViewDate: TextView = findViewById(R.id.textViewDate)
        val textViewDayMonth: TextView = findViewById(R.id.textViewDayMonth)

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        textViewDate.text = dateFormat.format(calendar.time)
        textViewDayMonth.text = "${dayFormat.format(calendar.time)}\n${monthYearFormat.format(calendar.time)}"
    }

    private fun updateCompletionPercentage() {
        val textViewCompletion: TextView = findViewById(R.id.textViewCompletion)
        val completedTasks = tasks.count { it.isCompleted }
        val percentage = if (tasks.isNotEmpty()) (completedTasks.toFloat() / tasks.size) * 100 else 0f
        textViewCompletion.text = String.format("%.2f%%\nCompleted", percentage)
    }

    inner class TaskAdapter(private val tasks: MutableList<Task>) :
        RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

        inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val checkBoxTask: CheckBox = itemView.findViewById(R.id.checkBoxTask)
            val textViewTask: TextView = itemView.findViewById(R.id.textViewTask)
            val buttonEdit: ImageButton = itemView.findViewById(R.id.buttonEdit)
            val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_task, parent, false)
            return TaskViewHolder(view)
        }

        override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
            val task = tasks[position]
            holder.textViewTask.text = task.value
            holder.checkBoxTask.isChecked = task.isCompleted

            updateTaskTextAppearance(holder.textViewTask, task.isCompleted)

            holder.checkBoxTask.setOnCheckedChangeListener(null) // Remove previous listener

            holder.checkBoxTask.setOnCheckedChangeListener { _, isChecked ->
                task.isCompleted = isChecked
                dbHelper.updateTask(task)
                updateTaskTextAppearance(holder.textViewTask, isChecked)
                updateCompletionPercentage()
            }

            holder.buttonEdit.setOnClickListener {
                showEditDialog(task, position)
            }

            holder.buttonDelete.setOnClickListener {
                showDeleteConfirmationDialog(position)
            }
        }

        private fun showEditDialog(task: Task, position: Int) {
            val editText = EditText(this@ToDoListActivity)
            editText.setText(task.value)

            // Create a container layout for padding
            val container = FrameLayout(this@ToDoListActivity)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(48, 16, 32, 16) // Add padding (left, top, right, bottom)
            editText.layoutParams = params
            container.addView(editText)

            val dialog = AlertDialog.Builder(this@ToDoListActivity)
                .setTitle("Edit Task")
                .setView(container)
                .setPositiveButton("Save") { _, _ ->
                    val editedText = editText.text.toString().trim()
                    if (editedText.isNotEmpty()) {
                        task.value = editedText
                        dbHelper.updateTask(task)
                        notifyItemChanged(position)
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()

            // Set corner radius for the dialog

            dialog.show()
        }

        private fun showDeleteConfirmationDialog(position: Int) {
            AlertDialog.Builder(this@ToDoListActivity)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete") { _, _ ->
                    val task = tasks[position]
                    dbHelper.deleteTask(task.id)
                    tasks.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, tasks.size)
                    updateCompletionPercentage()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun updateTaskTextAppearance(textView: TextView, isCompleted: Boolean) {
            if (isCompleted) {
                textView.paintFlags = textView.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textView.paintFlags = textView.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        override fun getItemCount() = tasks.size
    }

    // Modify the TaskItemDecoration class
    private class TaskItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: android.graphics.Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.top = spacing / 2
            outRect.bottom = spacing / 2
            outRect.left = spacing
            outRect.right = spacing
        }
    }

    data class Task(var id: Int, var value: String, var isCompleted: Boolean = false)
}
