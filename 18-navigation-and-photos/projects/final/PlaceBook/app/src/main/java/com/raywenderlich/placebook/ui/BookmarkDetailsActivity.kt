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

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.databinding.ActivityBookmarkDetailsBinding
import com.raywenderlich.placebook.util.ImageUtils
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import java.io.File

class BookmarkDetailsActivity : AppCompatActivity(),
    PhotoOptionDialogFragment.PhotoOptionDialogListener {

  private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
  private var bookmarkDetailsView:
      BookmarkDetailsViewModel.BookmarkDetailsView? = null
  private lateinit var databinding: ActivityBookmarkDetailsBinding
  private var photoFile: File? = null

  override fun onCaptureClick() {

    photoFile = null
    try {

      photoFile = ImageUtils.createUniqueImageFile(this)

    } catch (ex: java.io.IOException) {
      return
    }

    photoFile?.let { photoFile ->

      val photoUri = FileProvider.getUriForFile(this,
          "com.raywenderlich.placebook.fileprovider",
          photoFile)

      val captureIntent =
          Intent(MediaStore.ACTION_IMAGE_CAPTURE)

      captureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
          photoUri)

      val intentActivities = packageManager.queryIntentActivities(
          captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
      intentActivities.map { it.activityInfo.packageName }
          .forEach { grantUriPermission(it, photoUri,
              Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }

      startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)

    }

  }

  override fun onPickClick() {
    val pickIntent = Intent(Intent.ACTION_PICK,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
  }

  override fun onCreate(savedInstanceState:
                        android.os.Bundle?) {
    super.onCreate(savedInstanceState)
    databinding = DataBindingUtil.setContentView(this, R.layout.activity_bookmark_details)
    setupToolbar()
    getIntentData()
  }

  override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
    menuInflater.inflate(R.menu.menu_bookmark_details, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_save -> {
        saveChanges()
        return true
      }
      else -> return super.onOptionsItemSelected(item)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int,
                                data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == android.app.Activity.RESULT_OK) {

      when (requestCode) {

        REQUEST_CAPTURE_IMAGE -> {

          val photoFile = photoFile ?: return

          val uri = FileProvider.getUriForFile(this,
              "com.raywenderlich.placebook.fileprovider",
              photoFile)
          revokeUriPermission(uri,
              Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

          val image = getImageWithPath(photoFile.absolutePath)
          image?.let {
            val bitmap = ImageUtils.rotateImageIfRequired(this, it, uri)
            updateImage(bitmap)
          }
        }

        REQUEST_GALLERY_IMAGE -> if (data != null && data.data != null) {
          val imageUri = data.data as Uri
          val image = getImageWithAuthority(imageUri)
          image?.let {
            val bitmap = ImageUtils.rotateImageIfRequired(this, it, imageUri)
            updateImage(bitmap)
          }
        }
      }
    }
  }

  private fun getImageWithAuthority(uri: Uri): Bitmap? {
    return ImageUtils.decodeUriStreamToSize(uri,
        resources.getDimensionPixelSize(
            R.dimen.default_image_width),
        resources.getDimensionPixelSize(
            R.dimen.default_image_height),
        this)
  }

  private fun updateImage(image: Bitmap) {
    val bookmarkView = bookmarkDetailsView ?: return
    databinding.imageViewPlace.setImageBitmap(image)
    bookmarkView.setImage(this, image)
  }

  private fun getImageWithPath(filePath: String): Bitmap? {
    return ImageUtils.decodeFileToSize(filePath,
        resources.getDimensionPixelSize(
            R.dimen.default_image_width),
        resources.getDimensionPixelSize(
            R.dimen.default_image_height))
  }

  private fun replaceImage() {
    val newFragment = PhotoOptionDialogFragment.newInstance(this)
    newFragment?.show(supportFragmentManager, "photoOptionDialog")
  }

  private fun saveChanges() {
    val name = databinding.editTextName.text.toString()
    if (name.isEmpty()) {
      return
    }
    bookmarkDetailsView?.let { bookmarkView ->
      bookmarkView.name = databinding.editTextName.text.toString()
      bookmarkView.notes = databinding.editTextNotes.text.toString()
      bookmarkView.address = databinding.editTextAddress.text.toString()
      bookmarkView.phone = databinding.editTextPhone.text.toString()
      bookmarkDetailsViewModel.updateBookmark(bookmarkView)
    }
    finish()
  }

  private fun getIntentData() {

    val bookmarkId = intent.getLongExtra(
        MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0)

    bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(
        this, {

      it?.let {
        bookmarkDetailsView = it
        databinding.bookmarkDetailsView = it
        populateImageView()
      }
    })
  }

  private fun setupToolbar() {
    setSupportActionBar(databinding.toolbar)
  }

  private fun populateImageView() {
    bookmarkDetailsView?.let { bookmarkView ->
      val placeImage = bookmarkView.getImage(this)
      placeImage?.let {
        databinding.imageViewPlace.setImageBitmap(placeImage)
      }
    }
    databinding.imageViewPlace.setOnClickListener {
      replaceImage()
    }
  }

  companion object {
    private const val REQUEST_CAPTURE_IMAGE = 1
    private const val REQUEST_GALLERY_IMAGE = 2
  }
}
