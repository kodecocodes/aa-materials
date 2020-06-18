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

package com.raywenderlich.podplay.ui

import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.format.DateUtils
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.service.PodplayMediaCallback
import com.raywenderlich.podplay.service.PodplayMediaCallback.Companion.CMD_CHANGESPEED
import com.raywenderlich.podplay.service.PodplayMediaCallback.Companion.CMD_EXTRA_SPEED
import com.raywenderlich.podplay.service.PodplayMediaService
import com.raywenderlich.podplay.util.HtmlUtils
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import kotlinx.android.synthetic.main.fragment_episode_player.*

class EpisodePlayerFragment : Fragment() {

  private val podcastViewModel: PodcastViewModel by activityViewModels()
  private lateinit var mediaBrowser: MediaBrowserCompat
  private var mediaControllerCallback: MediaControllerCallback? = null
  private var playerSpeed: Float = 1.0f
  private var episodeDuration: Long = 0
  private var draggingScrubber: Boolean = false
  private var progressAnimator: ValueAnimator? = null
  private var mediaSession: MediaSessionCompat? = null
  private var mediaPlayer: MediaPlayer? = null
  private var isVideo: Boolean = false
  private var playOnPrepare: Boolean = false

  companion object {
    fun newInstance(): EpisodePlayerFragment {
      return EpisodePlayerFragment()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    retainInstance = true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      isVideo = podcastViewModel.activeEpisodeViewData?.isVideo ?: false
    } else {
      isVideo = false
    }
    if (!isVideo) {
      initMediaBrowser()
    }
  }
  override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_episode_player,
        container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setupControls()
    if (isVideo) {
      initMediaSession()
      initVideoPlayer()
    }
    updateControls()
  }

  override fun onStart() {
    super.onStart()
    if (!isVideo) {
      if (mediaBrowser.isConnected) {
        val fragmentActivity = activity as FragmentActivity
        if (MediaControllerCompat.getMediaController(fragmentActivity) == null) {
          registerMediaController(mediaBrowser.sessionToken)
        }
        updateControlsFromController()
      } else {
        mediaBrowser.connect()
      }
    }
  }

  override fun onStop() {
    super.onStop()
    progressAnimator?.cancel()
    val fragmentActivity = activity as FragmentActivity
    if (MediaControllerCompat.getMediaController(fragmentActivity) != null) {
      mediaControllerCallback?.let {
        MediaControllerCompat.getMediaController(fragmentActivity)
            .unregisterCallback(it)
      }
    }
    if (isVideo) {
      mediaPlayer?.setDisplay(null)
    }
    if (!fragmentActivity.isChangingConfigurations) {
      mediaPlayer?.release()
      mediaPlayer = null
    }
  }

  private fun setupControls() {
    playToggleButton.setOnClickListener {
      togglePlayPause()
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      speedButton.setOnClickListener {
        changeSpeed()
      }
    } else {
      speedButton.visibility = View.INVISIBLE
    }
    forwardButton.setOnClickListener {
      seekBy(30)
    }
    replayButton.setOnClickListener {
      seekBy(-10)
    }

    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        currentTimeTextView.text = DateUtils.formatElapsedTime((progress / 1000).toLong())
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {
        draggingScrubber = true
      }

      override fun onStopTrackingTouch(seekBar: SeekBar) {
        draggingScrubber = false
        val fragmentActivity = activity as FragmentActivity
        val controller = MediaControllerCompat.getMediaController(fragmentActivity)
        if (controller.playbackState != null) {
          controller.transportControls.seekTo(seekBar.progress.toLong())
        } else {
          seekBar.progress = 0
        }
      }
    })
  }

  private fun setupVideoUI() {
    episodeDescTextView.visibility = View.INVISIBLE
    headerView.visibility = View.INVISIBLE
    val activity = activity as AppCompatActivity
    activity.supportActionBar?.hide()
    playerControls.setBackgroundColor(Color.argb(255/2, 0, 0, 0))
  }

  private fun updateControlsFromController() {
    val fragmentActivity = activity as FragmentActivity
    val controller = MediaControllerCompat.getMediaController(fragmentActivity)
    if (controller != null) {
      val metadata = controller.metadata
      if (metadata != null) {
        handleStateChange(controller.playbackState.state,
            controller.playbackState.position, playerSpeed)
        updateControlsFromMetadata(controller.metadata)
      }
    }
  }

  private fun initVideoPlayer() {
    videoSurfaceView.visibility = View.VISIBLE
    val surfaceHolder = videoSurfaceView.holder
    surfaceHolder.addCallback(object: SurfaceHolder.Callback {
      override fun surfaceCreated(holder: SurfaceHolder) {
        initMediaPlayer()
        mediaPlayer?.setDisplay(holder)
      }

      override fun surfaceChanged(var1: SurfaceHolder, var2: Int, var3: Int, var4: Int) {
      }

      override fun surfaceDestroyed(var1: SurfaceHolder) {
      }
    })
  }

  private fun initMediaPlayer() {
    if (mediaPlayer == null) {
      mediaPlayer = MediaPlayer()
      mediaPlayer?.let {
        it.setAudioStreamType(AudioManager.STREAM_MUSIC)
        it.setDataSource(podcastViewModel.activeEpisodeViewData?.mediaUrl)
        it.setOnPreparedListener {
          val fragmentActivity = activity as FragmentActivity
          val episodeMediaCallback = PodplayMediaCallback(fragmentActivity, mediaSession!!, it)
          mediaSession!!.setCallback(episodeMediaCallback)
          setSurfaceSize()
          if (playOnPrepare) {
            togglePlayPause()
          }
        }
        it.prepareAsync()
      }
    } else {
      setSurfaceSize()
    }
  }

  private fun initMediaSession() {
    if (mediaSession == null) {
      mediaSession = MediaSessionCompat(activity as Context, "EpisodePlayerFragment")
      mediaSession?.setMediaButtonReceiver(null)
    }
    registerMediaController(mediaSession!!.sessionToken)
  }

  private fun setSurfaceSize() {

    val mediaPlayer = mediaPlayer ?: return

    val videoWidth = mediaPlayer.videoWidth
    val videoHeight = mediaPlayer.videoHeight

    val parent = videoSurfaceView.parent as View
    val containerWidth = parent.width
    val containerHeight = parent.height

    val layoutAspectRatio = containerWidth.toFloat() / containerHeight
    val videoAspectRatio = videoWidth.toFloat() / videoHeight

    val layoutParams = videoSurfaceView.layoutParams

    if (videoAspectRatio > layoutAspectRatio) {
      layoutParams.height = (containerWidth / videoAspectRatio).toInt()
    } else {
      layoutParams.width = (containerHeight * videoAspectRatio).toInt()
    }

    videoSurfaceView.layoutParams = layoutParams
  }

  private fun animateScrubber(progress: Int, speed: Float) {

    val timeRemaining = ((episodeDuration - progress) / speed).toInt()
    if (timeRemaining < 0) {
      return;
    }

    progressAnimator = ValueAnimator.ofInt(
        progress, episodeDuration.toInt())
    progressAnimator?.let { animator ->
      animator.duration = timeRemaining.toLong()
      animator.interpolator = LinearInterpolator()
      animator.addUpdateListener {
        if (draggingScrubber) {
          animator.cancel()
        } else {
          seekBar.progress = animator.animatedValue as Int
        }
      }
      animator.start()
    }
  }

  private fun updateControlsFromMetadata(metadata: MediaMetadataCompat) {
    episodeDuration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
    endTimeTextView.text = DateUtils.formatElapsedTime((episodeDuration / 1000))
    seekBar.max = episodeDuration.toInt()
  }

  private fun changeSpeed() {

    playerSpeed += 0.25f
    if (playerSpeed > 2.0f) {
      playerSpeed = 0.75f
    }

    val bundle = Bundle()
    bundle.putFloat(CMD_EXTRA_SPEED, playerSpeed)

    val fragmentActivity = activity as FragmentActivity
    val controller = MediaControllerCompat.getMediaController(fragmentActivity)
    controller.sendCommand(CMD_CHANGESPEED, bundle, null)

    speedButton.text = "${playerSpeed}x"
  }

  private fun seekBy(seconds: Int) {
    val fragmentActivity = activity as FragmentActivity
    val controller = MediaControllerCompat.getMediaController(fragmentActivity)
    val newPosition = controller.playbackState.position + seconds*1000
    controller.transportControls.seekTo(newPosition)
  }

  private fun handleStateChange(state: Int, position: Long, speed: Float) {
    progressAnimator?.let {
      it.cancel()
      progressAnimator = null
    }
    val isPlaying = state == PlaybackStateCompat.STATE_PLAYING
    playToggleButton.isActivated = isPlaying

    val progress = position.toInt()
    seekBar.progress = progress
    speedButton.text = "${playerSpeed}x"

    if (isPlaying) {
      if (isVideo) {
        setupVideoUI()
      }
      animateScrubber(progress, speed)
    }
  }

  private fun updateControls() {
    episodeTitleTextView.text = podcastViewModel.activeEpisodeViewData?.title

    val htmlDesc = podcastViewModel.activeEpisodeViewData?.description ?: ""
    val descSpan = HtmlUtils.htmlToSpannable(htmlDesc)
    episodeDescTextView.text = descSpan
    episodeDescTextView.movementMethod = ScrollingMovementMethod()

    val fragmentActivity = activity as FragmentActivity
    Glide.with(fragmentActivity)
        .load(podcastViewModel.activePodcastViewData?.imageUrl)
        .into(episodeImageView)

    speedButton.text = "${playerSpeed}x"
    
    mediaPlayer?.let {
      updateControlsFromController()
    }
  }

  private fun startPlaying(episodeViewData: PodcastViewModel.EpisodeViewData) {
    val fragmentActivity = activity as FragmentActivity
    val controller = MediaControllerCompat.getMediaController(fragmentActivity)

    val viewData = podcastViewModel.activePodcastViewData ?: return
    val bundle = Bundle()

    bundle.putString(MediaMetadataCompat.METADATA_KEY_TITLE, episodeViewData.title)
    bundle.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, viewData.feedTitle)
    bundle.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, viewData.imageUrl)

    controller.transportControls.playFromUri(Uri.parse(episodeViewData.mediaUrl), bundle)
  }
  
  private fun togglePlayPause() {
    playOnPrepare = true
    val fragmentActivity = activity as FragmentActivity
    val controller = MediaControllerCompat.getMediaController(fragmentActivity)
    if (controller.playbackState != null) {
      if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
        controller.transportControls.pause()
      } else {
        podcastViewModel.activeEpisodeViewData?.let { startPlaying(it) }
      }
    } else {
      podcastViewModel.activeEpisodeViewData?.let { startPlaying(it) }
    }
  }

  private fun registerMediaController(token: MediaSessionCompat.Token) {
    val mediaController = MediaControllerCompat(activity, token)
    val fragmentActivity = activity as FragmentActivity
    MediaControllerCompat.setMediaController(fragmentActivity, mediaController)
    mediaControllerCallback = MediaControllerCallback()
    mediaController.registerCallback(mediaControllerCallback!!)
  }

  private fun initMediaBrowser() {
    val fragmentActivity = activity as FragmentActivity
    mediaBrowser = MediaBrowserCompat(fragmentActivity,
        ComponentName(fragmentActivity, PodplayMediaService::class.java),
        MediaBrowserCallBacks(),
        null)
  }

  inner class MediaBrowserCallBacks: MediaBrowserCompat.ConnectionCallback() {

    override fun onConnected() {
      super.onConnected()
      registerMediaController(mediaBrowser.sessionToken)
      updateControlsFromController()
    }

    override fun onConnectionSuspended() {
      super.onConnectionSuspended()
      println("onConnectionSuspended")
    }

    override fun onConnectionFailed() {
      super.onConnectionFailed()
      println("onConnectionFailed")
    }
  }

  inner class MediaControllerCallback: MediaControllerCompat.Callback() {

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
      super.onMetadataChanged(metadata)
      metadata?.let { updateControlsFromMetadata(it) }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
      val state = state ?: return
      handleStateChange(state.state, state.position, state.playbackSpeed)
    }
  }
}
