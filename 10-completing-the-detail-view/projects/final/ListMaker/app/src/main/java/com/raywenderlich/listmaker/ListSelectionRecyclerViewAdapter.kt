package com.raywenderlich.listmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ListSelectionRecyclerViewAdapter(
  val lists: ArrayList<TaskList>,
  val clickListener: ListSelectionRecyclerViewClickListener
) : RecyclerView.Adapter<ListSelectionViewHolder>() {

  interface ListSelectionRecyclerViewClickListener {
    fun listItemClicked(list: TaskList)
  }

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
    holder.itemView.setOnClickListener {
      clickListener.listItemClicked(lists[position])
    }
  }

  fun addList(list: TaskList) {
    // 1
    lists.add(list)

    // 2
    notifyItemInserted(lists.size - 1)
  }
}
