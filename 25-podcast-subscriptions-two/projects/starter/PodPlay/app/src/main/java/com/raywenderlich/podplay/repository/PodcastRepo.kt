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

package com.raywenderlich.podplay.repository

import androidx.lifecycle.LiveData
import com.raywenderlich.podplay.db.PodcastDao
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.service.FeedService
import com.raywenderlich.podplay.service.RssFeedResponse
import com.raywenderlich.podplay.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastRepo(private var feedService: FeedService,
                  private var podcastDao: PodcastDao) {

  fun getPodcast(feedUrl: String, callback: (Podcast?) -> Unit) {

    GlobalScope.launch {

      val podcastLocal = podcastDao.loadPodcast(feedUrl)

      if (podcastLocal != null) {
        podcastLocal.id?.let {
          podcastLocal.episodes = podcastDao.loadEpisodes(it)
          GlobalScope.launch(Dispatchers.Main) {
            callback(podcastLocal)
          }
        }
      } else {
        feedService.getFeed(feedUrl) { feedResponse ->
          var podcast: Podcast? = null
          if (feedResponse != null) {
            podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
          }
          GlobalScope.launch(Dispatchers.Main) {
            callback(podcast)
          }
        }
      }
    }

  }

  fun getAll(): LiveData<List<Podcast>>
  {
    return podcastDao.loadPodcasts()
  }

  fun save(podcast: Podcast) {
    GlobalScope.launch {
      val podcastId = podcastDao.insertPodcast(podcast)
      for (episode in podcast.episodes) {
        episode.podcastId = podcastId
        podcastDao.insertEpisode(episode)
      }
    }
  }

  fun delete(podcast: Podcast) {
    GlobalScope.launch {
      podcastDao.deletePodcast(podcast)
    }
  }

  private fun rssResponseToPodcast(feedUrl: String, imageUrl: String, rssResponse:
  RssFeedResponse): Podcast? {

    val items = rssResponse.episodes ?: return null
    val description = if (rssResponse.description == "") rssResponse.summary else rssResponse.description

    return Podcast(null, feedUrl, rssResponse.title, description, imageUrl,
        rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
  }

  private fun rssItemsToEpisodes(episodeResponses: List<RssFeedResponse.EpisodeResponse>): List<Episode> {
    return episodeResponses.map {
      Episode(
          it.guid ?: "",
          null,
          it.title ?: "",
          it.description ?: "",
          it.url ?: "",
          it.type ?: "",
          DateUtils.xmlDateToDate(it.pubDate),
          it.duration ?: ""
      )
    }
  }

}
