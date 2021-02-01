package com.raywenderlich.podplay.service

import okhttp3.*
import java.io.IOException

class RssFeedService : FeedService {
  override suspend fun getFeed(xmlFileURL: String) {
    // 1
    val client = OkHttpClient()
// 2
    val request = Request.Builder()
        .url(xmlFileURL)
        .build()
// 3
    client.newCall(request).enqueue(object : Callback {
      // 4
      override fun onFailure(call: Call, e: IOException) {

      }

      // 5
      @Throws(IOException::class)
      override fun onResponse(call: Call, response: Response) {
// 6
        if (response.isSuccessful) {
          // 7
          response.body()?.let { responseBody ->
// 8
            println(responseBody.string())
            // Parse response and send to callback
            return
          }
        }
// 9

      }
    })
  }
}

interface FeedService {
  // 1
  suspend fun getFeed(xmlFileURL: String)

  // 2
  companion object {
    val instance: FeedService by lazy {
      RssFeedService()
    }
  }
}