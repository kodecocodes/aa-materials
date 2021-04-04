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

package com.raywenderlich.podplay.ui

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.EpisodeListAdapter
import com.raywenderlich.podplay.databinding.FragmentPodcastDetailsBinding
import com.raywenderlich.podplay.viewmodel.PodcastViewModel

class PodcastDetailsFragment : Fragment(), EpisodeListAdapter.EpisodeListAdapterListener {

  private val podcastViewModel: PodcastViewModel by activityViewModels()
  private lateinit var databinding: FragmentPodcastDetailsBinding
  private lateinit var episodeListAdapter: EpisodeListAdapter
  private var listener: OnPodcastDetailsListener? = null

  companion object {
    fun newInstance(): PodcastDetailsFragment {
      return PodcastDetailsFragment()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View {
    databinding = FragmentPodcastDetailsBinding.inflate(inflater, container, false)
    return databinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    podcastViewModel.podcastLiveData.observe(viewLifecycleOwner, { viewData ->
      if (viewData != null) {
        databinding.feedTitleTextView.text = viewData.feedTitle
        databinding.feedDescTextView.text = viewData.feedDesc
        activity?.let { activity ->
          Glide.with(activity).load(viewData.imageUrl).into(databinding.feedImageView)
        }

        // 1
        databinding.feedDescTextView.movementMethod = ScrollingMovementMethod()
        // 2
        databinding.episodeRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(activity)
        databinding.episodeRecyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            databinding.episodeRecyclerView.context, layoutManager.orientation)
        databinding.episodeRecyclerView.addItemDecoration(dividerItemDecoration)
        // 3
        episodeListAdapter = EpisodeListAdapter(viewData.episodes, this)
        databinding.episodeRecyclerView.adapter = episodeListAdapter

        activity?.invalidateOptionsMenu()
      }
    })
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnPodcastDetailsListener) {
      listener = context
    } else {
      throw RuntimeException(context.toString() +
          " must implement OnPodcastDetailsListener")
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater.inflate(R.menu.menu_details, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.menu_feed_action -> {
        if (item.title == getString(R.string.unsubscribe)) {
          listener?.onUnsubscribe()
        } else {
          listener?.onSubscribe()
        }
        true
      }
      else ->
        super.onOptionsItemSelected(item)
    }
  }

  override fun onPrepareOptionsMenu(menu: Menu) {
    podcastViewModel.podcastLiveData.observe(viewLifecycleOwner, { podcast ->
      if (podcast != null) {
        menu.findItem(R.id.menu_feed_action).title = if (podcast.subscribed)
          getString(R.string.unsubscribe) else getString(R.string.subscribe)
      }
    })

    super.onPrepareOptionsMenu(menu)
  }

  interface OnPodcastDetailsListener {
    fun onSubscribe()
    fun onUnsubscribe()
    fun onShowEpisodePlayer(episodeViewData: PodcastViewModel.EpisodeViewData)
  }

  override fun onSelectedEpisode(episodeViewData: PodcastViewModel.EpisodeViewData) {
    listener?.onShowEpisodePlayer(episodeViewData)
  }
}
