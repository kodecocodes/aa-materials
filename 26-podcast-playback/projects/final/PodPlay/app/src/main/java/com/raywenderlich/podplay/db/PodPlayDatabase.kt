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

package com.raywenderlich.podplay.db

import android.content.Context
import androidx.room.*
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import kotlinx.coroutines.CoroutineScope
import java.util.*

class Converters {
  @TypeConverter
  fun fromTimestamp(value: Long?): Date? {
    return if (value == null) null else Date(value)
  }

  @TypeConverter
  fun toTimestamp(date: Date?): Long? {
    return (date?.time)
  }
}

// 1
@Database(entities = [Podcast::class, Episode::class], version = 1)
@TypeConverters(Converters::class)
abstract class PodPlayDatabase : RoomDatabase() {
  // 2
  abstract fun podcastDao(): PodcastDao

  // 3
  companion object {

    // 4
    @Volatile
    private var INSTANCE: PodPlayDatabase? = null

    // 5
    fun getInstance(context: Context, coroutineScope: CoroutineScope): PodPlayDatabase {
      val tempInstance = INSTANCE
      if (tempInstance != null) {
        return tempInstance
      }

      // 6
      synchronized(this) {
        val instance = Room.databaseBuilder(context.applicationContext,
            PodPlayDatabase::class.java,
            "PodPlayer")
            .build()
        INSTANCE = instance
        // 7
        return instance
      }
    }
  }
}
