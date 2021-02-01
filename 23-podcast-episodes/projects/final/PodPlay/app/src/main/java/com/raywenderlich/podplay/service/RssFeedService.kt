package com.raywenderlich.podplay.service

class RssFeedService : FeedService {
  override fun getFeed(xmlFileURL: String,
                       callBack: (RssFeedResponse?) -> Unit) {
  }
}

interface FeedService {
  // 1
  fun getFeed(xmlFileURL: String,
              callBack: (RssFeedResponse?) -> Unit)

  // 2
  companion object {
    val instance: FeedService by lazy {
      RssFeedService()
    }
  }
}