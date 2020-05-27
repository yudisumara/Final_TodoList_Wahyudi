package com.wahyudi.todolist

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wahyudi.todolist.utils.Commons
import com.wahyudi.todolist.utils.InputDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_todo.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var todoViewModel: TodoViewModel
    private lateinit var todoAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutManager = LinearLayoutManager(this)
        recyclerview.layoutManager = layoutManager

        todoAdapter = TodoAdapter(){ todo, _ ->
            val options = resources.getStringArray(R.array.option_edit_delete)
            Commons.showSelector(this, "Choose action", options) { _, i ->
                when (i) {
                    0 -> showDetailsDialog(todo)
                    1 -> showEditDialog(todo)
                    2 -> showDeleteDialog(todo)
                }
            }
        }

        recyclerview.adapter = todoAdapter

        todoViewModel = ViewModelProvider(this).get(TodoViewModel::class.java)

        swipe_refresh_layout.setOnRefreshListener {
            refreshData()
        }

        fab.setOnClickListener {
            showInsertDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        observeData()
    }

    private fun observeData(){
        todoViewModel.getTodos()?.observe(this, Observer {
            todoAdapter.setTodoList(it)
            setProgressbarVisibility(false)
        })
    }

    private fun refreshData(){
        setProgressbarVisibility(true)
        observeData()
        swipe_refresh_layout.isRefreshing = false
    }

    private fun showInsertDialog(){
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_todo, null)

        view.input_due_date.setOnClickListener {
            Commons.showDatePickerDialog(this, view.input_due_date)
        }

        view.input_time.setOnClickListener {
            Commons.showTimePickerDialog(this, view.input_time)
        }

        val dialogTitle = "Add data"
        val toastMessage = "Data has been added successfully"
        val failAlertMessage = "Please fill all the required fields"

        InputDialog(this, dialogTitle, view){
            val title = view.input_title.text.toString().trim()
            val date = view.input_due_date.text.toString().trim()
            val time = view.input_time.text.toString().trim()
            val note = view.input_note.text.toString()

            val remindMe = view.input_remind_me.isChecked

            if (title == "" || date == "" || time == "") {
                AlertDialog.Builder(this).setMessage(failAlertMessage).setCancelable(false)
                    .setPositiveButton("OK") { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }.create().show()
            } else {
                val parsedDate = Commons.convertStringToDate("dd/MM/yy",date)
                val dueDate = Commons.formatDate(parsedDate, "dd/MM/yy")

                val currentDate = Commons.getCurrentDateTime()
                val dateCreated =Commons.formatDate(currentDate, "dd/MM/yy HH:mm:ss")

                val todo = Todo(
                    title = title,
                    note = note,
                    dateCreated = dateCreated,
                    dateUpdated = dateCreated,
                    dueDate = dueDate,
                    dueTime = time,
                    remindMe = remindMe
                )

                todoViewModel.insertTodo(todo)

                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    private fun showEditDialog(todo: Todo) {
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_todo, null)

        view.input_due_date.setOnClickListener {
            Commons.showDatePickerDialog(this, view.input_due_date)
        }

        view.input_time.setOnClickListener {
            Commons.showTimePickerDialog(this, view.input_time)
        }

        view.input_title.setText(todo.title)
        view.input_note.setText(todo.note)
        view.input_due_date.setText(todo.dueDate)
        view.input_time.setText(todo.dueTime)
        view.input_remind_me.isChecked = todo.remindMe

        val dialogTitle = "Edit data"
        val toastMessage = "Data has been updated successfully"
        val failAlertMessage = "Please fill all the required fields"

        InputDialog(this, dialogTitle, view){
            val title = view.input_title.text.toString().trim()
            val date = view.input_due_date.text.toString().trim()
            val time = view.input_time.text.toString().trim()
            val note = view.input_note.text.toString()

            val dateCreated = todo.dateCreated
            val remindMe = view.input_remind_me.isChecked

            if (title == "" || date == "" || time == "") {
                AlertDialog.Builder(this).setMessage(failAlertMessage).setCancelable(false)
                    .setPositiveButton("OK") { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }.create().show()
            } else {
                val parsedDate = Commons.convertStringToDate("dd/MM/yy",date)
                val dueDate = Commons.formatDate(parsedDate, "dd/MM/yy")

                val currentDate = Commons.getCurrentDateTime()
                val dateUpdated =Commons.formatDate(currentDate, "dd/MM/yy HH:mm:ss")

                todo.title = title
                todo.note = note
                todo.dateCreated = dateCreated
                todo.dateUpdated = dateUpdated
                todo.dueDate = dueDate
                todo.dueTime = time
                todo.remindMe = remindMe

                todoViewModel.updateTodo(todo)

                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    private fun showDeleteDialog(todo: Todo) {
        todoViewModel.deleteTodo(todo)
        Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun showDetailsDialog(todo: Todo) {
        val title = "Title: ${todo.title}"
        val dueDate = "Due date : ${todo.dueDate}, ${todo.dueTime}"
        val note = "Note: ${todo.note}"
        val dateCreated = "Date created: ${todo.dateCreated}"
        val dateUpdated = "Date updated: ${todo.dateUpdated}"

        val strReminder = if(todo.remindMe) "Enabled" else "Disabled"
        val remindMe = "Reminder: $strReminder"

        val strMessage = "$title\n$dueDate\n$note\n\n$dateCreated\n$dateUpdated\n$remindMe"

        AlertDialog.Builder(this).setMessage(strMessage).setCancelable(false)
            .setPositiveButton("OK") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create().show()
    }

    private fun setProgressbarVisibility(state: Boolean) {
        if (state) progressbar.visibility = View.VISIBLE
        else progressbar.visibility = View.INVISIBLE
    }
}
