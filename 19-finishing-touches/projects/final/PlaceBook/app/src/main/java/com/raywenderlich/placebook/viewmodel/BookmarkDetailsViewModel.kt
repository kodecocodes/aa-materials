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

package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookmarkDetailsViewModel(application: Application) : AndroidViewModel(application) {

  private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
  private var bookmarkDetailsView: LiveData<BookmarkDetailsView>? = null

  fun getCategories(): List<String> {
    return bookmarkRepo.categories
  }

  fun getBookmark(bookmarkId: Long): LiveData<BookmarkDetailsView>? {
    if (bookmarkDetailsView == null) {
      mapBookmarkToBookmarkView(bookmarkId)
    }
    return bookmarkDetailsView
  }

  fun updateBookmark(bookmarkDetailsView: BookmarkDetailsView) {
    GlobalScope.launch {
      val bookmark = bookmarkViewToBookmark(bookmarkDetailsView)
      bookmark?.let { bookmarkRepo.updateBookmark(it) }
    }
  }

  fun deleteBookmark(bookmarkDetailsView: BookmarkDetailsView) {
    GlobalScope.launch {
      val bookmark = bookmarkDetailsView.id?.let {
        bookmarkRepo.getBookmark(it)
      }
      bookmark?.let {
        bookmarkRepo.deleteBookmark(it)
      }
    }
  }

  fun getCategoryResourceId(category: String): Int? {
    return bookmarkRepo.getCategoryResourceId(category)
  }

  private fun bookmarkViewToBookmark(bookmarkDetailsView: BookmarkDetailsView):
      Bookmark? {
    val bookmark = bookmarkDetailsView.id?.let {
      bookmarkRepo.getBookmark(it)
    }
    if (bookmark != null) {
      bookmark.id = bookmarkDetailsView.id
      bookmark.name = bookmarkDetailsView.name
      bookmark.phone = bookmarkDetailsView.phone
      bookmark.address = bookmarkDetailsView.address
      bookmark.notes = bookmarkDetailsView.notes
      bookmark.category = bookmarkDetailsView.category
    }
    return bookmark
  }

  private fun mapBookmarkToBookmarkView(bookmarkId: Long) {
    val bookmark = bookmarkRepo.getLiveBookmark(bookmarkId)
    bookmarkDetailsView = Transformations.map(bookmark) { repoBookmark ->
      repoBookmark?.let {
        bookmarkToBookmarkView(repoBookmark)
      }
    }
  }

  private fun bookmarkToBookmarkView(bookmark: Bookmark): BookmarkDetailsView {
    return BookmarkDetailsView(
        bookmark.id,
        bookmark.name,
        bookmark.phone,
        bookmark.address,
        bookmark.notes,
        bookmark.category,
        bookmark.longitude,
        bookmark.latitude,
        bookmark.placeId
    )
  }

  data class BookmarkDetailsView(var id: Long? = null,
                                 var name: String = "",
                                 var phone: String = "",
                                 var address: String = "",
                                 var notes: String = "",
                                 var category: String = "",
                                 var longitude: Double = 0.0,
                                 var latitude: Double = 0.0,
                                 var placeId: String? = null) {
    fun getImage(context: Context): Bitmap? {
      id?.let {
        return ImageUtils.loadBitmapFromFile(context,
            Bookmark.generateImageFilename(it))
      }
      return null
    }

    fun setImage(context: Context, image: Bitmap) {
      id?.let {
        ImageUtils.saveBitmapToFile(context, image,
            Bookmark.generateImageFilename(it))
      }
    }
  }

}
