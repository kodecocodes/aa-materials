package com.raywenderlich.listmaker.ui.detail.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.listmaker.databinding.ListItemViewHolderBinding
import com.raywenderlich.listmaker.models.TaskList

class ListItemsRecyclerViewAdapter(var list: TaskList) : RecyclerView.Adapter<ListItemViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
    val binding = ListItemViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ListItemViewHolder(binding)
  }

  override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
    holder.binding.textViewTask.text = list.tasks[position]
  }

  override fun getItemCount(): Int {
    return list.tasks.size
  }

  fun tasksUpdated() {
    notifyItemInserted(list.tasks.size - 1)
  }
}