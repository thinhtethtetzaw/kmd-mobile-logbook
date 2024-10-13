package com.example.logbook

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_VALUE = "value"
        private const val COLUMN_COMPLETED = "completed"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_VALUE TEXT,
                $COLUMN_COMPLETED INTEGER
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addTask(task: ToDoListActivity.Task): Long {
        val values = ContentValues().apply {
            put(COLUMN_VALUE, task.value)
            put(COLUMN_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        return writableDatabase.insert(TABLE_NAME, null, values)
    }

    fun updateTask(task: ToDoListActivity.Task) {
        val values = ContentValues().apply {
            put(COLUMN_VALUE, task.value)
            put(COLUMN_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        writableDatabase.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
    }

    fun deleteTask(taskId: Int) {
        writableDatabase.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(taskId.toString()))
    }

    fun getAllTasks(): List<ToDoListActivity.Task> {
        val tasks = mutableListOf<ToDoListActivity.Task>()
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = readableDatabase.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val value = it.getString(it.getColumnIndexOrThrow(COLUMN_VALUE))
                val completed = it.getInt(it.getColumnIndexOrThrow(COLUMN_COMPLETED)) == 1
                tasks.add(ToDoListActivity.Task(id, value, completed))
            }
        }
        return tasks
    }
}
