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

package com.raywenderlich.podplay.service

import com.raywenderlich.podplay.BuildConfig
import com.raywenderlich.podplay.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.w3c.dom.Node
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory

class RssFeedService private constructor() {

  suspend fun getFeed(xmlFileURL: String): RssFeedResponse? {
    val service: FeedService

    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY

    val client = OkHttpClient().newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)

    if (BuildConfig.DEBUG) {
      client.addInterceptor(interceptor)
    }
    client.build()

    val retrofit = Retrofit.Builder()
        .baseUrl("${xmlFileURL.split("?")[0]}/")
        .build()
    service = retrofit.create(FeedService::class.java)

    try {
      val result = service.getFeed(xmlFileURL)
      if (result.code() >= 400) {
        println("server error, ${result.code()}, ${result.errorBody()}")
        return null
      } else {
        var rssFeedResponse: RssFeedResponse?
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        withContext(Dispatchers.IO) {
          val doc = dBuilder.parse(result.body()?.byteStream())
          val rss = RssFeedResponse(episodes = mutableListOf())
          domToRssFeedResponse(doc, rss)
          println(rss)
          rssFeedResponse = rss
        }
        return rssFeedResponse
      }
    } catch (t: Throwable) {
      println("error, ${t.localizedMessage}")
    }
    return null
  }

  private fun domToRssFeedResponse(node: Node, rssFeedResponse: RssFeedResponse) {
    if (node.nodeType == Node.ELEMENT_NODE) {
      val nodeName = node.nodeName
      val parentName = node.parentNode.nodeName
      // 1
      val grandParentName = node.parentNode.parentNode?.nodeName ?: ""
      // 2
      if (parentName == "item" && grandParentName == "channel") {
        // 3
        val currentItem = rssFeedResponse.episodes?.last()
        if (currentItem != null) {
          // 4
          when (nodeName) {
            "title" -> currentItem.title = node.textContent
            "description" -> currentItem.description = node.textContent
            "itunes:duration" -> currentItem.duration = node.textContent
            "guid" -> currentItem.guid = node.textContent
            "pubDate" -> currentItem.pubDate = node.textContent
            "link" -> currentItem.link = node.textContent
            "enclosure" -> {
              currentItem.url = node.attributes.getNamedItem("url")
                  .textContent
              currentItem.type = node.attributes.getNamedItem("type")
                  .textContent
            }
          }
        }
      }
      if (parentName == "channel") {
        when (nodeName) {
          "title" -> rssFeedResponse.title = node.textContent
          "description" -> rssFeedResponse.description = node.textContent
          "itunes:summary" -> rssFeedResponse.summary = node.textContent
          "item" -> rssFeedResponse.episodes?.add(RssFeedResponse.EpisodeResponse())
          "pubDate" -> rssFeedResponse.lastUpdated =
              DateUtils.xmlDateToDate(node.textContent)
        }
      }
    }
    val nodeList = node.childNodes
    for (i in 0 until nodeList.length) {
      val childNode = nodeList.item(i)
      domToRssFeedResponse(childNode, rssFeedResponse)
    }
  }
  companion object {
    val instance: RssFeedService by lazy {
      RssFeedService()
    }
  }
}

interface FeedService {
  @Headers(
      "Content-Type: application/xml; charset=utf-8",
      "Accept: application/xml"
  )
  @GET
  suspend fun getFeed(@Url xmlFileURL: String): Response<ResponseBody>
}