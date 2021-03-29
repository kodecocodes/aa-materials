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

package com.raywenderlich.podplay.repository

import androidx.lifecycle.LiveData
import com.raywenderlich.podplay.db.PodcastDao
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.service.RssFeedResponse
import com.raywenderlich.podplay.service.RssFeedService
import com.raywenderlich.podplay.util.DateUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastRepo(private var feedService: RssFeedService,
                  private var podcastDao: PodcastDao) {

  suspend fun getPodcast(feedUrl: String): Podcast? {
    val podcastLocal = podcastDao.loadPodcast(feedUrl)
    if (podcastLocal != null) {
      podcastLocal.id?.let {
        podcastLocal.episodes = podcastDao.loadEpisodes(it)
        return podcastLocal
      }
    }
    var podcast: Podcast? = null
    val feedResponse = feedService.getFeed(feedUrl)
    if (feedResponse != null) {
      podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
    }
    return podcast
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

  private fun rssResponseToPodcast(
      feedUrl: String, imageUrl: String, rssResponse: RssFeedResponse
  ): Podcast? {
    // 1
    val items = rssResponse.episodes ?: return null
    // 2
    val description = if (rssResponse.description == "")
      rssResponse.summary else rssResponse.description
    // 3
    return Podcast(null, feedUrl, rssResponse.title, description, imageUrl,
        rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
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

  fun getAll(): LiveData<List<Podcast>> {
    return podcastDao.loadPodcasts()
  }

  suspend fun updatePodcastEpisodes() : MutableList<PodcastUpdateInfo> {
    // 1
    val updatedPodcasts: MutableList<PodcastUpdateInfo> = mutableListOf()
    // 2
    val podcasts = podcastDao.loadPodcastsStatic()
    // 3
    for (podcast in podcasts) {
      // 4
      val newEpisodes = getNewEpisodes(podcast)
      // 5
      if (newEpisodes.count() > 0) {
        podcast.id?.let {
          saveNewEpisodes(it, newEpisodes)
          updatedPodcasts.add(PodcastUpdateInfo(podcast.feedUrl, podcast.feedTitle, newEpisodes.count()))
        }
      }
    }
    // 6
    return updatedPodcasts
  }

  private suspend fun getNewEpisodes(localPodcast: Podcast): List<Episode> {
    // 1
    val response = feedService.getFeed(localPodcast.feedUrl)
    if (response != null) {
      // 2
      val remotePodcast = rssResponseToPodcast(localPodcast.feedUrl, localPodcast.imageUrl, response)
      remotePodcast?.let {
        // 3
        val localEpisodes = podcastDao.loadEpisodes(localPodcast.id!!)
        // 4
        return remotePodcast.episodes.filter { episode ->
          localEpisodes.find { episode.guid == it.guid } == null
        }
      }
    }
    // 6
    return listOf()
  }

  private fun saveNewEpisodes(podcastId: Long, episodes: List<Episode>) {
    GlobalScope.launch {
      for (episode in episodes) {
        episode.podcastId = podcastId
        podcastDao.insertEpisode(episode)
      }
    }
  }

  class PodcastUpdateInfo(val feedUrl: String, val name: String, val newCount: Int)
}
