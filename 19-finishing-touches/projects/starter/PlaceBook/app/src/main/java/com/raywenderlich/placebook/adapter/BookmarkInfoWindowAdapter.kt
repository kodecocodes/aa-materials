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

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel

class BookmarkInfoWindowAdapter(val context: Activity) :
    GoogleMap.InfoWindowAdapter {

  private val contents: View

  init {
    contents = context.layoutInflater.inflate(
        R.layout.content_bookmark_info, null)
  }

  override fun getInfoWindow(marker: Marker): View? {
    // This function is required, but can return null if
    // not replacing the entire info window
    return null
  }

  override fun getInfoContents(marker: Marker): View? {
    val titleView = contents.findViewById<TextView>(R.id.title)
    titleView.text = marker.title ?: ""

    val phoneView = contents.findViewById<TextView>(R.id.phone)
    phoneView.text = marker.snippet ?: ""

    val imageView = contents.findViewById<ImageView>(R.id.photo)
    when (marker.tag) {

      is MapsActivity.PlaceInfo -> {
        imageView.setImageBitmap(
            (marker.tag as MapsActivity.PlaceInfo).image)
      }

      is MapsViewModel.BookmarkView -> {
        var bookMarkview = marker.tag as
            MapsViewModel.BookmarkView
        // Set imageView bitmap here
        imageView.setImageBitmap(bookMarkview.getImage(context))
      }
    }
    return contents
  }
}
