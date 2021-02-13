package com.raywenderlich.podplay.service

import com.raywenderlich.podplay.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory

class RssFeedService {

  suspend fun getFeed(xmlFileURL: String): Boolean {
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
        .baseUrl("$xmlFileURL/")
//        .addConverterFactory(JaxbConverterFactory.create())
//        .addConverterFactory(Xml.asConverterFactory(contentType))
        .build()
    service = retrofit.create(FeedService::class.java)

    try {
      val result = service.getFeed("$xmlFileURL/")
      if (result.code() >= 400) {
        // TODO : // create an error from error body and return
        println("server error, ${result.code()}, ${result.errorBody()}")
        return false
      } else {
        // return success result
//        println(result.body()?.string())
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(result.body()?.byteStream())
        return true
      }
    } catch (t: Throwable) {
      // TODO : create an error from throwable and return it
      println("error, ${t.localizedMessage}")
    }
    return false
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