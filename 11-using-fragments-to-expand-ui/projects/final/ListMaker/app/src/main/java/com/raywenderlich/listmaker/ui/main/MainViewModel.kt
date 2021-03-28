package com.raywenderlich.listmaker.ui.main

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.raywenderlich.listmaker.models.TaskList

class MainViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {

  lateinit var onListAdded: (() -> Unit)

  lateinit var onTaskAdded: (() -> Unit)

  lateinit var list: TaskList

  val lists: MutableList<TaskList> by lazy {
    retrieveLists()
  }

  fun addTask(task: String) {
    list.tasks.add(task)
    updateList(list)
  }

  private fun retrieveLists(): MutableList<TaskList> {

    val sharedPreferencesContents = sharedPreferences.all
    val taskLists = ArrayList<TaskList>()

    for (taskList in sharedPreferencesContents) {
      val itemsHashSet = ArrayList(taskList.value as HashSet<String>)
      val list = TaskList(taskList.key, itemsHashSet)
      taskLists.add(list)
    }

    return taskLists
  }

  fun saveList(list: TaskList) {
    sharedPreferences.edit().putStringSet(list.name, list.tasks.toHashSet()).apply()
    lists.add(list)
    onListAdded.invoke()
  }

  fun updateList(list: TaskList) {
    sharedPreferences.edit().putStringSet(list.name, list.tasks.toHashSet()).apply()
  }

  fun refreshLists() {
    lists.clear()
    lists.addAll(retrieveLists())
  }
}