package com.raywenderlich.listmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ListSelectionRecyclerViewAdapter(private val lists: ArrayList<TaskList>) : RecyclerView.Adapter<ListSelectionViewHolder>() {
  
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListSelectionViewHolder {
    // 1
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.list_selection_view_holder,
        parent,
        false)
    // 2
    return ListSelectionViewHolder(view)
  }

  override fun getItemCount(): Int {
    return lists.size
  }

  override fun onBindViewHolder(holder: ListSelectionViewHolder, position: Int) {
    holder.listPosition.text = (position + 1).toString()
    holder.listTitle.text = lists.get(position).name
  }

  fun addList(list: TaskList) {
    // 1
    lists.add(list)

    // 2
    notifyItemInserted(lists.size - 1)
  }
}
