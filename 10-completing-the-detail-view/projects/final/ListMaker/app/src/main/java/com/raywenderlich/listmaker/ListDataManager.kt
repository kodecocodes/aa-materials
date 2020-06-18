package com.raywenderlich.listmaker

import android.content.Context
import androidx.preference.PreferenceManager


class ListDataManager(private val context: Context) {
  fun saveList(list: TaskList) {
    // 1
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit()
    // 2
    sharedPreferences.putStringSet(list.name, list.tasks.toHashSet())
    // 3
    sharedPreferences.apply()
  }

  fun readLists(): ArrayList<TaskList> {
    // 1
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    // 2
    val sharedPreferenceContents = sharedPreferences.all
    // 3
    val taskLists = ArrayList<TaskList>()

    // 4
    for (taskList in sharedPreferenceContents) {

      val itemsHashSet = ArrayList(taskList.value as HashSet<String>)
      val list = TaskList(taskList.key, itemsHashSet)
      // 5
      taskLists.add(list)
    }

    // 6
    return taskLists
  }
}