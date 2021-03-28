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

package com.raywenderlich.podplay.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.db.PodPlayDatabase
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.service.RssFeedService
import com.raywenderlich.podplay.ui.PodcastActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class EpisodeUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
  // 1
  override suspend fun doWork(): Result = coroutineScope {
    // 2
    val job = async {
      // 3
      val db = PodPlayDatabase.getInstance(applicationContext, this)
      val repo = PodcastRepo(RssFeedService.instance, db.podcastDao())
      // 4
      val podcastUpdates = repo.updatePodcastEpisodes()
        // 5
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          createNotificationChannel()
        }
        // 6
        for (podcastUpdate in podcastUpdates) {
          displayNotification(podcastUpdate)
        }
    }
    // 7
    job.await()
    // 8
    Result.success()
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel() {
    val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (notificationManager.getNotificationChannel(EPISODE_CHANNEL_ID) == null) {
      val channel = NotificationChannel(EPISODE_CHANNEL_ID, "Episodes", NotificationManager.IMPORTANCE_DEFAULT)
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun displayNotification(podcastInfo: PodcastRepo.PodcastUpdateInfo) {

    val contentIntent = Intent(applicationContext, PodcastActivity::class.java)
    contentIntent.putExtra(EXTRA_FEED_URL, podcastInfo.feedUrl)
    val pendingContentIntent = PendingIntent.getActivity(applicationContext, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val notification = NotificationCompat.Builder(applicationContext, EPISODE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_episode_icon)
        .setContentTitle(applicationContext.getString(R.string.episode_notification_title))
        .setContentText(applicationContext.getString(R.string.episode_notification_text, podcastInfo.newCount, podcastInfo.name))
        .setNumber(podcastInfo.newCount)
        .setAutoCancel(true)
        .setContentIntent(pendingContentIntent)
        .build()

    val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(podcastInfo.name, 0, notification)
  }

  companion object {
    const val EPISODE_CHANNEL_ID = "podplay_episodes_channel"
    const val EXTRA_FEED_URL = "PodcastFeedUrl"
  }
}