package com.wahyudi.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.wahyudi.todolist.utils.Commons
import kotlinx.android.synthetic.main.item_empty.view.*
import kotlinx.android.synthetic.main.item_todo.view.*
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(private val listener: (Todo, Int) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val VIEW_NO_DATA = 0
    private val VIEW_TODO = 1

    private var todoList = listOf<Todo>()
    private var todoSearchResult = listOf<Todo>()

    fun setTodoList(todoList: List<Todo>) {
        this.todoList = todoList
        this.todoSearchResult = todoList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val keywords = constraint.toString()
                if (keywords.isEmpty())
                    todoSearchResult = todoList
                else{
                    val filteredList = ArrayList<Todo>()
                    for (todo in todoList) {
                        if (todo.toString().toLowerCase(Locale.ROOT).contains(keywords.toLowerCase(Locale.ROOT)))
                            filteredList.add(todo)
                    }
                    todoSearchResult = filteredList
                }

                val searchResults = FilterResults()
                searchResults.values = todoSearchResult
                return  searchResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                todoSearchResult = results?.values as List<Todo>
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (todoSearchResult.isEmpty())
            VIEW_NO_DATA
        else
            VIEW_TODO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder{
        return when (viewType) {
            VIEW_TODO -> TodoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false))
            VIEW_NO_DATA -> EmptyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false))
            else -> throw throw IllegalArgumentException("Undefined view type")
        }
    }

    override fun getItemCount(): Int = if (todoSearchResult.isEmpty()) 1 else todoSearchResult.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_NO_DATA -> {
                val emptyHolder = holder as EmptyViewHolder
                emptyHolder.bindItem()
            }
            VIEW_TODO -> {
                val todoHolder = holder as TodoViewHolder
                val sortedList = todoSearchResult.sortedWith(
                    if(MainActivity.isSortByDateCreated)
                        compareBy({it.dateCreated}, {it.dateUpdated})
                    else{
                        compareBy({it.dueDate}, {it.dueTime})
                    })
                todoHolder.bindItem(sortedList[position], listener)
            }
        }
    }

    class TodoViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        fun bindItem(todo: Todo, listener: (Todo, Int) -> Unit) {
            val parsedDateCreated = SimpleDateFormat("dd/MM/yy", Locale.US).parse(todo.dateCreated) as Date
            val dateCreated = Commons.formatDate(parsedDateCreated, "dd MMM yyyy")

            val parsedDateUpdated = SimpleDateFormat("dd/MM/yy", Locale.US).parse(todo.dateCreated) as Date
            val dateUpdated = Commons.formatDate(parsedDateUpdated, "dd MMM yyyy")

            val date = if (todo.dateUpdated != todo.dateCreated) "Updated at $dateUpdated" else "Created at $dateCreated"

            val parsedDate = SimpleDateFormat("dd/MM/yy", Locale.US).parse(todo.dueDate) as Date
            val dueDate = Commons.formatDate(parsedDate, "dd MMM yyyy")

            val dueDateTime = "Due ${dueDate}, ${todo.dueTime}"

            itemView.tv_title.text = todo.title
            itemView.tv_note.text = todo.note
            itemView.tv_due_date.text = dueDateTime
            itemView.tv_date_created_updated.text = date

            itemView.setOnClickListener{
                listener(todo, layoutPosition)
            }
        }
    }

    class EmptyViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        fun bindItem(){
            itemView.tv_empty.text = "Belum ada data"
        }
    }
}