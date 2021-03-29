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
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils

class MapsViewModel(application: Application) : AndroidViewModel(application) {

  private val TAG = "MapsViewModel"

  private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
  private var bookmarks: LiveData<List<BookmarkView>>? = null

  fun addBookmark(latLng: LatLng) : Long? {
    val bookmark = bookmarkRepo.createBookmark()
    bookmark.name = "Untitled"
    bookmark.longitude = latLng.longitude
    bookmark.latitude = latLng.latitude
    bookmark.category = "Other"
    return bookmarkRepo.addBookmark(bookmark)
  }

  fun addBookmarkFromPlace(place: Place, image: Bitmap?) {
    val bookmark = bookmarkRepo.createBookmark()
    bookmark.placeId = place.id
    bookmark.name = place.name.toString()
    bookmark.longitude = place.latLng?.longitude ?: 0.0
    bookmark.latitude = place.latLng?.latitude ?: 0.0
    bookmark.phone = place.phoneNumber.toString()
    bookmark.address = place.address.toString()
    bookmark.category = getPlaceCategory(place)

    val newId = bookmarkRepo.addBookmark(bookmark)
    image?.let { bookmark.setImage(it, getApplication()) }
    Log.i(TAG, "New bookmark $newId added to the database.")
  }

  fun getBookmarkViews(): LiveData<List<BookmarkView>>? {
    if (bookmarks == null) {
      mapBookmarksToBookmarkView()
    }
    return bookmarks
  }

  private fun mapBookmarksToBookmarkView() {
    bookmarks = Transformations.map(bookmarkRepo.allBookmarks) { repoBookmarks ->
      repoBookmarks.map { bookmark ->
        bookmarkToBookmarkView(bookmark)
      }
    }
  }

  private fun getPlaceCategory(place: Place): String {

    var category = "Other"
    val types = place.types

    types?.let { placeTypes ->
      if (placeTypes.size > 0) {
        val placeType = placeTypes[0]
        category = bookmarkRepo.placeTypeToCategory(placeType)
      }
    }

    return category
  }

  private fun bookmarkToBookmarkView(bookmark: Bookmark): BookmarkView {
    return BookmarkView(
        bookmark.id,
        LatLng(bookmark.latitude, bookmark.longitude),
        bookmark.name,
        bookmark.phone,
        bookmarkRepo.getCategoryResourceId(bookmark.category))
  }

  data class BookmarkView(val id: Long? = null,
                          val location: LatLng = LatLng(0.0, 0.0),
                          val name: String = "",
                          val phone: String = "",
                          val categoryResourceId: Int? = null) {
    fun getImage(context: Context): Bitmap? {
      id?.let {
        return ImageUtils.loadBitmapFromFile(context,
            Bookmark.generateImageFilename(it))
      }
      return null
    }
  }
}
