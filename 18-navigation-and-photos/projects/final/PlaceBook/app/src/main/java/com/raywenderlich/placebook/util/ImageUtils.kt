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

package com.raywenderlich.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


object ImageUtils {

  @Throws(IOException::class)
  fun createUniqueImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
    val filename = "PlaceBook_" + timeStamp + "_"
    val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(filename, ".jpg", filesDir)
  }

  fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String) {

    val stream = ByteArrayOutputStream()

    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

    val bytes = stream.toByteArray()

    saveBytesToFile(context, bytes, filename)
  }

  private fun saveBytesToFile(context: Context, bytes: ByteArray, filename: String) {
    val outputStream: FileOutputStream

    try {
      outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
      outputStream.write(bytes)
      outputStream.close()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun loadBitmapFromFile(context: Context, filename: String): Bitmap? {
    val filePath = File(context.filesDir, filename).absolutePath
    return BitmapFactory.decodeFile(filePath)
  }

  fun decodeFileToSize(filePath: String, width: Int, height: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(filePath, options)

    options.inSampleSize = calculateInSampleSize(
        options.outWidth, options.outHeight, width, height)

    options.inJustDecodeBounds = false

    return BitmapFactory.decodeFile(filePath, options)
  }

  fun decodeUriStreamToSize(
      uri: Uri,
      width: Int,
      height: Int,
      context: Context
  ): Bitmap? {
    var inputStream: InputStream? = null
    try {
      val options: BitmapFactory.Options
      inputStream = context.contentResolver.openInputStream(uri)
      if (inputStream != null) {
        options = BitmapFactory.Options()
        options.inJustDecodeBounds = false
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()
        inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
          options.inSampleSize = calculateInSampleSize(
              options.outWidth, options.outHeight,
              width, height)
          options.inJustDecodeBounds = false
          val bitmap = BitmapFactory.decodeStream(
              inputStream, null, options)
          inputStream.close()
          return bitmap
        }
      }
      return null
    } catch (e: Exception) {
      return null
    } finally {
      inputStream?.close()
    }
  }

  private fun calculateInSampleSize(
      width: Int,
      height: Int,
      reqWidth: Int,
      reqHeight: Int
  ): Int {
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
      val halfHeight = height / 2
      val halfWidth = width / 2
      while (halfHeight / inSampleSize >= reqHeight &&
          halfWidth / inSampleSize >= reqWidth) {
        inSampleSize *= 2
      }
    }
    return inSampleSize
  }

  private fun rotateImage(img: Bitmap, degree: Float): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(degree)
    val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    img.recycle()
    return rotatedImg
  }

  @Throws(IOException::class)
  fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
    val input: InputStream? = context.contentResolver.openInputStream(selectedImage)
    val path = selectedImage.path
    val ei: ExifInterface = when {
      Build.VERSION.SDK_INT > 23 && input != null -> ExifInterface(input)
      path != null -> ExifInterface(path)
      else -> null
    } ?: return img
    return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
      ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90.0f) ?: img
      ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180.0f) ?: img
      ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270.0f) ?: img
      else -> img
    }
  }
}
