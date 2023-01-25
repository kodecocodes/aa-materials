package com.raywenderlich.listmaker.ui.detail

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.raywenderlich.listmaker.MainActivity
import com.raywenderlich.listmaker.R
import com.raywenderlich.listmaker.databinding.ListDetailActivityBinding
import com.raywenderlich.listmaker.models.TaskList
import com.raywenderlich.listmaker.ui.detail.ui.detail.ListDetailFragment
import com.raywenderlich.listmaker.ui.detail.ui.detail.ListDetailViewModel

class ListDetailActivity : AppCompatActivity() {

  lateinit var binding: ListDetailActivityBinding

  lateinit var viewModel: ListDetailViewModel

  lateinit var fragment: ListDetailFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ListDetailActivityBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    viewModel = ViewModelProvider(this).get(ListDetailViewModel::class.java)
    viewModel.list = intent.getParcelableExtra(MainActivity.INTENT_LIST_KEY)!!

    binding.addTaskButton.setOnClickListener {
      showCreateTaskDialog()
    }

    title = viewModel.list.name

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.detail_container, ListDetailFragment.newInstance())
        .commitNow()
    }
  }

  private fun showCreateTaskDialog() {
    //1
    val taskEditText = EditText(this)
    taskEditText.inputType = InputType.TYPE_CLASS_TEXT

    //2
    AlertDialog.Builder(this)
            .setTitle(R.string.task_to_add)
            .setView(taskEditText)
            .setPositiveButton(R.string.add_task) { dialog, _ ->
              // 3
              val task = taskEditText.text.toString()
              // 4
              viewModel.addTask(task)
              //5
              dialog.dismiss()
            }
            //6
            .create()
            .show()
  }

  override fun onBackPressed() {
    val bundle = Bundle()
    bundle.putParcelable(MainActivity.INTENT_LIST_KEY, viewModel.list)

    val intent = Intent()
    intent.putExtras(bundle)
    setResult(Activity.RESULT_OK, intent)
    super.onBackPressed()
  }
}