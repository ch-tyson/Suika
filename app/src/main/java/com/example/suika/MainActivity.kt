package com.example.suika

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var scoreText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && !::gameView.isInitialized) {
            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            val rect = Rect()
            window.decorView.getWindowVisibleDisplayFrame(rect)
            val statusBarHeight = rect.top

            gameView = GameView(this, width, height - statusBarHeight)
            val rootLayout = findViewById<FrameLayout>(R.id.root_layout)
            rootLayout.addView(gameView, 0) // Add below scoreText

            scoreText = findViewById(R.id.score_text)

            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        updateModel()
                        updateView()
                        updateScore()
                    }
                }
            }, 0, 30)
        }
    }

    private fun updateScore() {
        val current = gameView.getGame().getScore()
        val best = gameView.getGame().getBestScore()
        scoreText.text = "🍉 Score: $current  |  Best: $best 🍓"
    }

    fun updateModel() {
        gameView.getGame().update()
    }

    fun updateView() {
        gameView.invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            gameView.getGame().setDropX(event.x.toInt())
            gameView.getGame().spawnFruit()
        }
        return super.onTouchEvent(event)
    }
}
