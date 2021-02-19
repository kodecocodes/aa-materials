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

package com.raywenderlich.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.viewmodel.SearchViewModel.PodcastSummaryViewData
import kotlinx.coroutines.launch
import java.util.*

class PodcastViewModel(application: Application) : AndroidViewModel(application) {

  var podcastRepo: PodcastRepo? = null
  private val _podcastLiveData = MutableLiveData<PodcastViewData?>()
  val podcastLiveData: LiveData<PodcastViewData?> = _podcastLiveData

  fun getPodcast(podcastSummaryViewData: PodcastSummaryViewData) {
    podcastSummaryViewData.feedUrl?.let { url ->
      viewModelScope.launch {
        podcastRepo?.getPodcast(url)?.let {
          it.feedTitle = podcastSummaryViewData.name ?: ""
          it.imageUrl = podcastSummaryViewData.imageUrl ?: ""
          _podcastLiveData.value = podcastToPodcastView(it)
        } ?: run {
          _podcastLiveData.value = null
        }
      }
    } ?: run {
      _podcastLiveData.value = null
    }
  }

  private fun podcastToPodcastView(podcast: Podcast): PodcastViewData {
    return PodcastViewData(false, podcast.feedTitle, podcast.feedUrl, podcast.feedDesc,
        podcast.imageUrl, episodesToEpisodesView(podcast.episodes))
  }

  private fun episodesToEpisodesView(episodes: List<Episode>): List<EpisodeViewData> {
    return episodes.map {
      EpisodeViewData(it.guid, it.title, it.description, it.mediaUrl, it.releaseDate, it.duration)
    }
  }

  data class PodcastViewData(var subscribed: Boolean = false, var feedTitle: String? = "",
                             var feedUrl: String? = "", var feedDesc: String? = "",
                             var imageUrl: String? = "", var episodes: List<EpisodeViewData>)

  data class EpisodeViewData(var guid: String? = "", var title: String? = "",
                             var description: String? = "", var mediaUrl: String? = "",
                             var releaseDate: Date? = null, var duration: String? = "")
}
