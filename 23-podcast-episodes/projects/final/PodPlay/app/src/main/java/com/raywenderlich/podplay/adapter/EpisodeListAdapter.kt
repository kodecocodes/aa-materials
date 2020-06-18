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

package com.raywenderlich.podplay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.util.DateUtils
import com.raywenderlich.podplay.util.HtmlUtils
import com.raywenderlich.podplay.viewmodel.PodcastViewModel.EpisodeViewData
import kotlinx.android.synthetic.main.episode_item.view.*

class EpisodeListAdapter(private var episodeViewList: List<EpisodeViewData>?) :
    RecyclerView.Adapter<EpisodeListAdapter.ViewHolder>() {

  class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    var episodeViewData: EpisodeViewData? = null
    val titleTextView: TextView = v.titleView
    val descTextView: TextView = v.descView
    val durationTextView: TextView = v.durationView
    val releaseDateTextView: TextView = v.releaseDateView
  }

  override fun onCreateViewHolder(parent: ViewGroup,
                                  viewType: Int): EpisodeListAdapter.ViewHolder {
    return ViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.episode_item, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val episodeViewList = episodeViewList ?: return
    val episodeView = episodeViewList[position]

    holder.episodeViewData = episodeView
    holder.titleTextView.text = episodeView.title
    holder.descTextView.text =  HtmlUtils.htmlToSpannable(episodeView.description ?: "")
    holder.durationTextView.text = episodeView.duration
    holder.releaseDateTextView.text = episodeView.releaseDate?.let { DateUtils.dateToShortDate(it) }
  }

  override fun getItemCount(): Int {
    return episodeViewList?.size ?: 0
  }
}
