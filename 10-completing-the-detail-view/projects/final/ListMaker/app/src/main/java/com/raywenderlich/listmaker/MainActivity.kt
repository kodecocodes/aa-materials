package com.raywenderlich.listmaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
  ListSelectionRecyclerViewAdapter.ListSelectionRecyclerViewClickListener {

  val listDataManager: ListDataManager = ListDataManager(this)

  lateinit var listsRecyclerView: RecyclerView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)

    fab.setOnClickListener {
      showCreateListDialog()
    }

    // 1
    val lists = listDataManager.readLists()
    listsRecyclerView = findViewById(R.id.lists_recyclerview)
    listsRecyclerView.layoutManager = LinearLayoutManager(this)

    // 2
    listsRecyclerView.adapter = ListSelectionRecyclerViewAdapter(lists, this)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    return when (item.itemId) {
      R.id.action_settings -> true
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun showCreateListDialog() {
    // 1
    val dialogTitle = getString(R.string.name_of_list)
    val positiveButtonTitle = getString(R.string.create_list)

    // 2
    val builder = AlertDialog.Builder(this)
    val listTitleEditText = EditText(this)
    listTitleEditText.inputType = InputType.TYPE_CLASS_TEXT

    builder.setTitle(dialogTitle)
    builder.setView(listTitleEditText)

    // 3
    builder.setPositiveButton(positiveButtonTitle) { dialog, _ ->
      val list = TaskList(listTitleEditText.text.toString())
      listDataManager.saveList(list)

      val recyclerAdapter = listsRecyclerView.adapter as ListSelectionRecyclerViewAdapter
      recyclerAdapter.addList(list)

      dialog.dismiss()
      showListDetail(list)
    }

    // 4
    builder.create().show()
  }

  private fun showListDetail(list: TaskList) {
    // 1
    val listDetailIntent = Intent(this, ListDetailActivity::class.java)
    // 2
    listDetailIntent.putExtra(INTENT_LIST_KEY, list)
    // 3
    startActivityForResult(listDetailIntent, LIST_DETAIL_REQUEST_CODE)
  }

  override fun listItemClicked(list: TaskList) {
    showListDetail(list)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    // 1
    if (requestCode == LIST_DETAIL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      // 2
      data?.let {
        // 3
        listDataManager.saveList(data.getParcelableExtra(INTENT_LIST_KEY) as TaskList)
        updateLists()
      }
    }
  }

  private fun updateLists() {
    val lists = listDataManager.readLists()
    listsRecyclerView.adapter =
      ListSelectionRecyclerViewAdapter(lists, this)
  }

  companion object {
    const val INTENT_LIST_KEY = "list"
    const val LIST_DETAIL_REQUEST_CODE = 123
  }
}
