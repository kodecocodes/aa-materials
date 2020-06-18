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

package com.raywenderlich.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.db.BookmarkDao
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

class BookmarkRepo(private val context: Context) {

  private var db: PlaceBookDatabase = PlaceBookDatabase.getInstance(context)
  private var bookmarkDao: BookmarkDao = db.bookmarkDao()
  private var categoryMap: HashMap<Place.Type, String> = buildCategoryMap()
  private var allCategories: HashMap<String, Int> = buildCategories()

  val categories: List<String>
    get() = ArrayList(allCategories.keys)


  fun updateBookmark(bookmark: Bookmark) {
    bookmarkDao.updateBookmark(bookmark)
  }

  fun getBookmark(bookmarkId: Long): Bookmark {
    return bookmarkDao.loadBookmark(bookmarkId)
  }

  fun addBookmark(bookmark: Bookmark): Long? {
    val newId = bookmarkDao.insertBookmark(bookmark)
    bookmark.id = newId
    return newId
  }

  fun createBookmark(): Bookmark {
    return Bookmark()
  }

  fun deleteBookmark(bookmark: Bookmark) {
    bookmark.deleteImage(context)
    bookmarkDao.deleteBookmark(bookmark)
  }


  fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> {
    val bookmark = bookmarkDao.loadLiveBookmark(bookmarkId)
    return bookmark
  }

  fun placeTypeToCategory(placeType: Place.Type): String {
    var category = "Other"
    if (categoryMap.containsKey(placeType)) {
      category = categoryMap[placeType].toString()
    }
    return category
  }

  val allBookmarks: LiveData<List<Bookmark>>
    get() {
      return bookmarkDao.loadAll()
    }

  fun getCategoryResourceId(placeCategory: String): Int? {
    return allCategories[placeCategory]
  }

  private fun buildCategories() : HashMap<String, Int> {
    return hashMapOf(
        "Gas" to R.drawable.ic_gas,
        "Lodging" to R.drawable.ic_lodging,
        "Other" to R.drawable.ic_other,
        "Restaurant" to R.drawable.ic_restaurant,
        "Shopping" to R.drawable.ic_shopping
    )
  }

  private fun buildCategoryMap() : HashMap<Place.Type, String> {
    return hashMapOf(
        Place.Type.BAKERY to "Restaurant",
        Place.Type.BAR to "Restaurant",
        Place.Type.CAFE to "Restaurant",
        Place.Type.FOOD to "Restaurant",
        Place.Type.RESTAURANT to "Restaurant",
        Place.Type.MEAL_DELIVERY to "Restaurant",
        Place.Type.MEAL_TAKEAWAY to "Restaurant",
        Place.Type.GAS_STATION to "Gas",
        Place.Type.CLOTHING_STORE to "Shopping",
        Place.Type.DEPARTMENT_STORE to "Shopping",
        Place.Type.FURNITURE_STORE to "Shopping",
        Place.Type.GROCERY_OR_SUPERMARKET to "Shopping",
        Place.Type.HARDWARE_STORE to "Shopping",
        Place.Type.HOME_GOODS_STORE to "Shopping",
        Place.Type.JEWELRY_STORE to "Shopping",
        Place.Type.SHOE_STORE to "Shopping",
        Place.Type.SHOPPING_MALL to "Shopping",
        Place.Type.STORE to "Shopping",
        Place.Type.LODGING to "Lodging",
        Place.Type.ROOM to "Lodging"
    )
  }
}
