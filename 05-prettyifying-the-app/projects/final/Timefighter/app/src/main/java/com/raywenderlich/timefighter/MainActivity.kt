package com.raywenderlich.timefighter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  private var score = 0

  private lateinit var gameScoreTextView: TextView
  private lateinit var timeLeftTextView: TextView
  private lateinit var tapMeButton: Button

  private var gameStarted = false

  private lateinit var countDownTimer: CountDownTimer
  private var initialCountDown: Long = 60000
  private var countDownInterval: Long = 1000
  private var timeLeft = 60

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    Log.d(TAG, "onCreate called. Score is: $score")

    // connect views to variables
    // 1
    gameScoreTextView = findViewById(R.id.game_score_text_view)
    timeLeftTextView = findViewById(R.id.time_left_text_view)
    tapMeButton = findViewById(R.id.tap_me_button)
    // 2
    tapMeButton.setOnClickListener { view ->
      val bounceAnimation = AnimationUtils.loadAnimation(this,
        R.anim.bounce)
      view.startAnimation(bounceAnimation)
      incrementScore()
    }

    if (savedInstanceState != null) {
      score = savedInstanceState.getInt(SCORE_KEY)
      timeLeft = savedInstanceState.getInt(TIME_LEFT_KEY)
      restoreGame()
    } else {
      resetGame()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {

    super.onSaveInstanceState(outState)

    outState.putInt(SCORE_KEY, score)
    outState.putInt(TIME_LEFT_KEY, timeLeft)
    countDownTimer.cancel()

    Log.d(TAG, "onSaveInstanceState: Saving Score: $score & Time Left: $timeLeft")
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    super.onCreateOptionsMenu(menu)
    menuInflater.inflate(R.menu.menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == R.id.about_item) {
      showInfo()
    }

    return true
  }

  override fun onDestroy() {
    super.onDestroy()

    Log.d(TAG, "onDestroy called.")
  }

  private fun showInfo() {
    val dialogTitle = getString(R.string.about_title,
      BuildConfig.VERSION_NAME)
    val dialogMessage = getString(R.string.about_message)

    val builder = AlertDialog.Builder(this)
    builder.setTitle(dialogTitle)
    builder.setMessage(dialogMessage)
    builder.create().show()
  }

  private fun incrementScore() {

    if (!gameStarted) {
      startGame()
    }

    score++

    val newScore = getString(R.string.your_score, score)
    gameScoreTextView.text = newScore
  }

  private fun resetGame() {
    // 1
    score = 0

    val initialScore = getString(R.string.your_score, score)
    gameScoreTextView.text = initialScore

    val initialTimeLeft = getString(R.string.time_left, 60)
    timeLeftTextView.text = initialTimeLeft

    // 2
    countDownTimer = object : CountDownTimer(initialCountDown, countDownInterval) {
      // 3
      override fun onTick(millisUntilFinished: Long) {
        timeLeft = millisUntilFinished.toInt() / 1000

        val timeLeftString = getString(R.string.time_left, timeLeft)
        timeLeftTextView.text = timeLeftString
      }

      override fun onFinish() {
        endGame()
      }
    }

    // 4
    gameStarted = false
  }

  private fun restoreGame() {

    val restoredScore = getString(R.string.your_score, score)
    gameScoreTextView.text = restoredScore

    val restoredTime = getString(R.string.time_left, timeLeft)
    timeLeftTextView.text = restoredTime

    countDownTimer = object : CountDownTimer((timeLeft * 1000).toLong(), countDownInterval) {
      override fun onTick(millisUntilFinished: Long) {

        timeLeft = millisUntilFinished.toInt() / 1000

        val timeLeftString = getString(R.string.time_left, timeLeft)
        timeLeftTextView.text = timeLeftString
      }

      override fun onFinish() {
        endGame()
      }
    }

    countDownTimer.start()
    gameStarted = true
  }

  private fun startGame() {
    countDownTimer.start()
    gameStarted = true
  }

  private fun endGame() {
    Toast.makeText(this, getString(R.string.game_over_message, score), Toast.LENGTH_LONG).show()
    resetGame()
  }

  companion object {
    private const val SCORE_KEY = "SCORE_KEY"
    private const val TIME_LEFT_KEY = "TIME_LEFT_KEY"
  }
}
