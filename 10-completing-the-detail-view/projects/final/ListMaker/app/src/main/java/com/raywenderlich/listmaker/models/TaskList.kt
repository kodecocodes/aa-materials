package com.raywenderlich.listmaker.models

import android.os.Parcel
import android.os.Parcelable

class TaskList(val name: String, val tasks: ArrayList<String> = ArrayList()) : Parcelable {

  //1
  constructor(source: Parcel) : this(
      source.readString()!!,
      source.createStringArrayList()!!
  )

  override fun describeContents() = 0

  //2
  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(name)
    dest.writeStringList(tasks)
  }

  // 3
  companion object CREATOR : Parcelable.Creator<TaskList> {
    // 4
    override fun createFromParcel(source: Parcel): TaskList = TaskList(source)
    override fun newArray(size: Int): Array<TaskList?> = arrayOfNulls(size)
  }
}