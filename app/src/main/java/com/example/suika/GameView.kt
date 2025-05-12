package com.example.suika

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

class GameView(context: Context, val screenWidth: Int, val screenHeight: Int) : View(context) {
    private val game = Game(context, screenWidth, screenHeight)
    private val paint = Paint()
    private val jarBitmap = BitmapFactory.decodeResource(resources, R.drawable.jar)
    private val jarRect: Rect

    init {
        paint.isAntiAlias = true

        // Keep image's height but stretch width fully
        val jarBitmapRatio = jarBitmap.height.toFloat() / jarBitmap.width
        val jarHeight = (screenWidth * jarBitmapRatio).toInt()
        val top = screenHeight - jarHeight
        jarRect = Rect(0, top, screenWidth, screenHeight)
    }

    fun getGame(): Game = game

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the jar background (full width, original image height preserved)
        canvas.drawBitmap(jarBitmap, null, jarRect, paint)

        // Draw all fruits
        for (fruit in game.getFruits()) {
            val bmp = game.getBitmap(fruit.type)
            bmp?.let {
                canvas.drawBitmap(it, null, fruit.getRect(), paint)
            }
        }
    }
}
