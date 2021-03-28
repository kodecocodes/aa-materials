/*
 * Copyright (c) 2021 Razeware LLC
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *   Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 *   distribute, sublicense, create a derivative work, and/or sell copies of the
 *   Software in any work that is designed, intended, or marketed for pedagogical or
 *   instructional purposes related to programming, coding, application development,
 *   or information technology.  Permission for such use, copying, modification,
 *   merger, publication, distribution, sublicensing, creation of derivative works,
 *   or sale is expressly withheld.
 *
 *   This project and source code may use libraries or frameworks that are
 *   released under various Open-Source licenses. Use of those libraries and
 *   frameworks are governed by their own individual licenses.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */

package com.raywenderlich.podplay.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.databinding.SearchItemBinding
import com.raywenderlich.podplay.viewmodel.SearchViewModel.PodcastSummaryViewData

class PodcastListAdapter(
    private var podcastSummaryViewList: List<PodcastSummaryViewData>?,
    private val podcastListAdapterListener: PodcastListAdapterListener,
    private val parentActivity: Activity
) : RecyclerView.Adapter<PodcastListAdapter.ViewHolder>() {

  interface PodcastListAdapterListener {
    fun onShowDetails(podcastSummaryViewData: PodcastSummaryViewData)
  }

  inner class ViewHolder(
      databinding: SearchItemBinding,
      private val podcastListAdapterListener: PodcastListAdapterListener
  ) : RecyclerView.ViewHolder(databinding.root) {
    var podcastSummaryViewData: PodcastSummaryViewData? = null
    val nameTextView: TextView = databinding.podcastNameTextView
    val lastUpdatedTextView: TextView = databinding.podcastLastUpdatedTextView
    val podcastImageView: ImageView = databinding.podcastImage

    init {
      databinding.searchItem.setOnClickListener {
        podcastSummaryViewData?.let {
          podcastListAdapterListener.onShowDetails(it)
        }
      }
    }
  }

  fun setSearchData(podcastSummaryViewData: List<PodcastSummaryViewData>) {
    podcastSummaryViewList = podcastSummaryViewData
    this.notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastListAdapter.ViewHolder {
    return ViewHolder(
        SearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        podcastListAdapterListener
    )
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val searchViewList = podcastSummaryViewList ?: return
    val searchView = searchViewList[position]
    holder.podcastSummaryViewData = searchView
    holder.nameTextView.text = searchView.name
    holder.lastUpdatedTextView.text = searchView.lastUpdated
    Glide.with(parentActivity).load(searchView.imageUrl).into(holder.podcastImageView)
  }

  override fun getItemCount(): Int {
    return podcastSummaryViewList?.size ?: 0
  }
}
