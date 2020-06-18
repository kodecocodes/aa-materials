package com.raywenderlich.timefighter

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

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

    // connect views to variables
    // 1
    gameScoreTextView = findViewById(R.id.game_score_text_view)
    timeLeftTextView = findViewById(R.id.time_left_text_view)
    tapMeButton = findViewById(R.id.tap_me_button)
    // 2
    tapMeButton.setOnClickListener { incrementScore() }

    resetGame()
  }

  private fun incrementScore() {

    if (!gameStarted) {
      startGame()
    }

    score++

    val newScore = "Your Score: $score"
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

  private fun startGame() {
    countDownTimer.start()
    gameStarted = true
  }

  private fun endGame() {
    Toast.makeText(this, getString(R.string.game_over_message, score), Toast.LENGTH_LONG).show()
    resetGame()
  }
}
