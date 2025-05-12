package com.example.suika

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.util.Log
import kotlin.math.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class Game(private val context: Context, private val screenWidth: Int, private val screenHeight: Int) {
    data class Fruit(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var radius: Float,
        var type: Int
    ) {
        fun getRect(): RectF {
            return RectF(x - radius, y - radius, x + radius, y + radius)
        }
    }

    private val gravity = 0.8f
    private val damping = 0.05f
    private val fruits = CopyOnWriteArrayList<Fruit>()
    private val bitmaps = mutableMapOf<Int, Bitmap>()
    private var dropX = screenWidth / 2f
    private val baseSize = screenWidth / 8f
    private val random = Random()

    // Scoring
    private var score = 0
    private val prefs = context.getSharedPreferences("suika_prefs", Context.MODE_PRIVATE)

    // Jar physics bounds
    private val jarInset = screenWidth * 0.25f
    private val jarLeft = jarInset
    private val jarRight = screenWidth - jarInset
    private val jarBottom = screenHeight.toFloat() * 1.6f
    private val curveDepth = 100f
    private val curveTop = screenHeight * 0.9f

    init {
        loadBitmaps()
    }

    fun getScore(): Int = score
    fun getBestScore(): Int = prefs.getInt("best_score", 0)

    fun setDropX(x: Int) {
        dropX = x.toFloat().coerceIn(jarLeft + baseSize, jarRight - baseSize)
    }

    fun spawnFruit() {
        val type = 1
        val radius = baseSize * 0.4f
        fruits.add(Fruit(dropX, 0f, 0f, 0f, radius, type))
    }

    fun getFruits(): List<Fruit> = fruits

    fun getBitmap(type: Int): Bitmap? = bitmaps[type]

    private fun loadBitmaps() {
        val typeToRes = mapOf(
            1 to R.drawable.blueberry_1,
            2 to R.drawable.strawberry_2,
            3 to R.drawable.orange_3,
            4 to R.drawable.apple_4,
            5 to R.drawable.pear_5,
            6 to R.drawable.peach_6,
            7 to R.drawable.melon_7,
            8 to R.drawable.watermelon_8,
            9 to R.drawable.jar
        )

        for ((type, resId) in typeToRes) {
            val scale = (1.23f).pow((type - 1).toFloat())
            val size = (baseSize * scale).toInt()
            val bmp = BitmapFactory.decodeResource(context.resources, resId)
            val scaled = Bitmap.createScaledBitmap(bmp, size, size, true)
            bitmaps[type] = scaled
        }
    }

    fun update() {
        for (fruit in fruits) {
            fruit.vy += gravity
            fruit.x += fruit.vx
            fruit.y += fruit.vy

            // Horizontal bounds
            if (fruit.x - fruit.radius < jarLeft) {
                fruit.x = jarLeft + fruit.radius
                fruit.vx *= -damping
            } else if (fruit.x + fruit.radius > jarRight) {
                fruit.x = jarRight - fruit.radius
                fruit.vx *= -damping
            }

            // Curved U-bottom
            val xRatio = ((fruit.x - jarLeft) / (jarRight - jarLeft)).coerceIn(0f, 1f)
            val jarCurveY = curveTop + (sin(xRatio * Math.PI).toFloat() * curveDepth)

            if (fruit.y + fruit.radius > jarCurveY) {
                fruit.y = jarCurveY - fruit.radius
                fruit.vy = -abs(fruit.vy) * damping
                if (abs(fruit.vy) < 0.2f) fruit.vy = 0f
            }
        }

        // Merging logic
        val toMerge = mutableSetOf<Pair<Fruit, Fruit>>()
        for (i in fruits.indices) {
            for (j in i + 1 until fruits.size) {
                val f1 = fruits[i]
                val f2 = fruits[j]
                if (f1.type == f2.type && f1.type < 8 && areColliding(f1, f2)) {
                    toMerge.add(f1 to f2)
                }
            }
        }

        for ((f1, f2) in toMerge) {
            fruits.remove(f1)
            fruits.remove(f2)

            val newType = f1.type + 1
            if (newType <= 8 && bitmaps.containsKey(newType)) {
                val scale = (1.23f).pow((newType - 1).toFloat())
                val newRadius = baseSize * 0.4f * scale
                val cx = (f1.x + f2.x) / 2
                val cy = (f1.y + f2.y) / 2 - newRadius
                fruits.add(Fruit(cx, cy, 0f, -3f, newRadius, newType))

                // Increase score
                score += 2.0.pow(f1.type.toDouble()).toInt()
                if (score > getBestScore()) {
                    prefs.edit().putInt("best_score", score).apply()
                }
            }
        }

        // Resolve overlaps
        for (i in fruits.indices) {
            for (j in i + 1 until fruits.size) {
                val f1 = fruits[i]
                val f2 = fruits[j]
                val dx = f2.x - f1.x
                val dy = f2.y - f1.y
                val dist = sqrt(dx * dx + dy * dy)
                val minDist = f1.radius + f2.radius
                if (dist < minDist && dist != 0f) {
                    val overlap = 0.5f * (minDist - dist)
                    val ox = overlap * (dx / dist)
                    val oy = overlap * (dy / dist)
                    f1.x -= ox
                    f1.y -= oy
                    f2.x += ox
                    f2.y += oy
                }
            }
        }
    }

    private fun areColliding(f1: Fruit, f2: Fruit): Boolean {
        val dx = f1.x - f2.x
        val dy = f1.y - f2.y
        val distanceSq = dx * dx + dy * dy
        val radiusSum = f1.radius + f2.radius
        return distanceSq < radiusSum * radiusSum
    }
}
