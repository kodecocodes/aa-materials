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

package com.raywenderlich.placebook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.databinding.BookmarkItemBinding
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel.BookmarkView

class BookmarkListAdapter(
    private var bookmarkData: List<BookmarkView>?,
    private val mapsActivity: MapsActivity) :
    RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {

  class ViewHolder(val binding: BookmarkItemBinding,
                   private val mapsActivity: MapsActivity) :
      RecyclerView.ViewHolder(binding.root) {

    init {
      binding.root.setOnClickListener {
        val bookmarkView = itemView.tag as BookmarkView
        mapsActivity.moveToBookmark(bookmarkView)
      }
    }
  }

  fun setBookmarkData(bookmarks: List<BookmarkView>) {
    this.bookmarkData = bookmarks
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(
      parent: ViewGroup,
      viewType: Int): ViewHolder {
    val layoutInflater = LayoutInflater.from(parent.context)
    val binding = BookmarkItemBinding.inflate(layoutInflater, parent, false)
    return ViewHolder(binding, mapsActivity)
  }

  override fun onBindViewHolder(holder: ViewHolder,
                                position: Int) {

    val bookmarkData = bookmarkData ?: return
    val bookmarkViewData = bookmarkData[position]

    holder.binding.root.tag = bookmarkViewData
    holder.binding.bookmarkData = bookmarkViewData
    holder.binding.bookmarkIcon.setImageResource(
        R.drawable.ic_other)
  }

  override fun getItemCount(): Int {
    return bookmarkData?.size ?: 0
  }
}
