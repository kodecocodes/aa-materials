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

package com.raywenderlich.podplay.service

import com.raywenderlich.podplay.util.DateUtils
import okhttp3.*
import org.w3c.dom.Node
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory

class RssFeedService: FeedService  {
  override fun getFeed(xmlFileURL: String, callBack: (RssFeedResponse?) -> Unit) {

    val client = OkHttpClient()

    val request = Request.Builder()
        .url(xmlFileURL)
        .build()

    client.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        callBack(null)
      }

      @Throws(IOException::class)
      override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
          response.body()?.let { responseBody ->
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(responseBody.byteStream())
            val rssFeedResponse = RssFeedResponse(episodes = mutableListOf())
            domToRssFeedResponse(doc, rssFeedResponse)
            callBack(rssFeedResponse)
            return
          }
        }
        callBack(null)
      }
    })
  }

  private fun domToRssFeedResponse(node: Node, rssFeedResponse: RssFeedResponse) {

    if (node.nodeType == Node.ELEMENT_NODE) {

      val nodeName = node.nodeName
      val parentName = node.parentNode.nodeName
      val grandParentName = node.parentNode.parentNode?.nodeName ?: ""

      if (parentName == "item" && grandParentName == "channel") {
        val currentItem = rssFeedResponse.episodes?.last()
        if (currentItem != null) {
          when (nodeName) {
            "title" -> currentItem.title = node.textContent
            "description" -> currentItem.description = node.textContent
            "itunes:duration" -> currentItem.duration = node.textContent
            "guid" -> currentItem.guid = node.textContent
            "pubDate" -> currentItem.pubDate = node.textContent
            "link" -> currentItem.link = node.textContent
            "enclosure" -> {
              currentItem.url = node.attributes.getNamedItem("url").textContent
              currentItem.type = node.attributes.getNamedItem("type").textContent
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
          "pubDate" -> rssFeedResponse.lastUpdated = DateUtils.xmlDateToDate(node.textContent)
        }
      }
    }

    val nodeList = node.childNodes

    for (i in 0 until nodeList.length) {
      val childNode = nodeList.item(i)
      domToRssFeedResponse(childNode, rssFeedResponse)
    }
  }
}

interface FeedService {
  fun getFeed(xmlFileURL: String, callBack: (RssFeedResponse?) -> Unit)

  companion object {
    val instance: FeedService by lazy {
      RssFeedService()
    }
  }
}
