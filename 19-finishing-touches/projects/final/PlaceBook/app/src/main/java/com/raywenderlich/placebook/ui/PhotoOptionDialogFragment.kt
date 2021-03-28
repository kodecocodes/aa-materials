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

package com.raywenderlich.placebook.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class PhotoOptionDialogFragment : DialogFragment() {

  interface PhotoOptionDialogListener {
    fun onCaptureClick()
    fun onPickClick()
  }

  private lateinit var listener: PhotoOptionDialogListener

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    listener = activity as PhotoOptionDialogListener
    var captureSelectIdx = -1
    var pickSelectIdx = -1
    val options = ArrayList<String>()
    val context = activity as Context

    if (canCapture(context)) {
      options.add("Camera")
      captureSelectIdx = 0
    }

    if (canPick(context)) {
      options.add("Gallery")
      pickSelectIdx = if (captureSelectIdx == 0) 1 else 0
    }

    return AlertDialog.Builder(context).setTitle("Photo Option").setItems(options.toTypedArray<CharSequence>()) { _, which ->
          if (which == captureSelectIdx) {
            listener.onCaptureClick()
          } else if (which == pickSelectIdx) {
            listener.onPickClick()
          }
        }.setNegativeButton("Cancel", null).create()
  }

  companion object {
    fun canPick(context: Context): Boolean {
      val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      return (pickIntent.resolveActivity(context.packageManager) != null)
    }

    fun canCapture(context: Context): Boolean {
      val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
      return (captureIntent.resolveActivity(context.packageManager) != null)
    }

    fun newInstance(context: Context): PhotoOptionDialogFragment? {
      return if (canPick(context) || canCapture(context)) {
        PhotoOptionDialogFragment()
      } else {
        null
      }
    }
  }
}
