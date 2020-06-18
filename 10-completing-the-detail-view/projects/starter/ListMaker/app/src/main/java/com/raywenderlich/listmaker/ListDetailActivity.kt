package com.raywenderlich.listmaker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ListDetailActivity : AppCompatActivity() {

  lateinit var list: TaskList

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_list_detail)
    // 1
    list = intent.getParcelableExtra(MainActivity.INTENT_LIST_KEY) as TaskList
    // 2
    title = list.name
  }
}
