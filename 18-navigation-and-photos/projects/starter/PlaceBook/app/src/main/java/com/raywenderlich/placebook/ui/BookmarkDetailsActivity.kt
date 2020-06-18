/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.placebook.ui

import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*

class BookmarkDetailsActivity : AppCompatActivity() {

  private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
  private var bookmarkDetailsView:
      BookmarkDetailsViewModel.BookmarkDetailsView? = null

  override fun onCreate(savedInstanceState:
                        android.os.Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bookmark_details)
    setupToolbar()
    getIntentData()
  }

  override fun onCreateOptionsMenu(menu: android.view.Menu):
      Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.menu_bookmark_details, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_save -> {
        saveChanges()
        return true
      }
      else -> return super.onOptionsItemSelected(item)
    }
  }

  private fun saveChanges() {
    val name = editTextName.text.toString()
    if (name.isEmpty()) {
      return
    }
    bookmarkDetailsView?.let { bookmarkView ->
      bookmarkView.name = editTextName.text.toString()
      bookmarkView.notes = editTextNotes.text.toString()
      bookmarkView.address = editTextAddress.text.toString()
      bookmarkView.phone = editTextPhone.text.toString()
      bookmarkDetailsViewModel.updateBookmark(bookmarkView)
    }
    finish()
  }

  private fun getIntentData() {

    val bookmarkId = intent.getLongExtra(
        MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0)

    bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(
        this, Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {

      it?.let {
        bookmarkDetailsView = it
        // Populate fields from bookmark
        populateFields()
        populateImageView()
      }
    })
  }

  private fun setupToolbar() {
    setSupportActionBar(toolbar)
  }

  private fun populateFields() {
    bookmarkDetailsView?.let { bookmarkView ->
      editTextName.setText(bookmarkView.name)
      editTextPhone.setText(bookmarkView.phone)
      editTextNotes.setText(bookmarkView.notes)
      editTextAddress.setText(bookmarkView.address)
    }
  }

  private fun populateImageView() {
    bookmarkDetailsView?.let { bookmarkView ->
      val placeImage = bookmarkView.getImage(this)
      placeImage?.let {
        imageViewPlace.setImageBitmap(placeImage)
      }
    }
  }

}
