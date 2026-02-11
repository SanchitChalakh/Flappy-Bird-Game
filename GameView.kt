package com.example.flappyface

import android.content.Context
import android.graphics.*
import android.view.SurfaceView
import android.widget.Toast
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), Runnable {

    private var thread: Thread? = null
    private var isPlaying = true

    private val screenWidth = resources.displayMetrics.widthPixels
    private val screenHeight = resources.displayMetrics.heightPixels

    // Player
    private val playerBitmap = BitmapFactory.decodeResource(resources, R.drawable.face1)
    private var playerX = 200
    private var playerY = screenHeight / 2
    private var velocity = 0
    private val gravity = 3

    // Background
    private val bgBitmap = BitmapFactory.decodeResource(resources, R.drawable.bg)
    private var bgX = 0
    private val bgSpeed = 5

    // Pipes
    private val pipeBitmap = BitmapFactory.decodeResource(resources, R.drawable.pipe)
    private val pipeWidth = pipeBitmap.width
    private val gap = 450
    private val pipes = ArrayList<Pipe>()

    // Score
    private var score = 0
    private val scorePaint = Paint().apply {
        color = Color.BLACK
        textSize = 80f
        typeface = Typeface.DEFAULT_BOLD
    }

    init {
        // Pipes
        pipes.add(Pipe(screenWidth + 200))
        pipes.add(Pipe(screenWidth + 600))
        pipes.add(Pipe(screenWidth + 1000))
    }

    override fun run() {
        while (isPlaying) {
            update()
            draw()
            Thread.sleep(17)
        }
    }

    private fun update() {
        // Gravity
        velocity += gravity
        playerY += velocity
        if (playerY > screenHeight - playerBitmap.height) playerY = screenHeight - playerBitmap.height
        if (playerY < 0) playerY = 0

        // Background scroll
        bgX -= bgSpeed
        if (bgX <= -screenWidth) bgX = 0

        // Pipes
        val iterator = pipes.iterator()
        while (iterator.hasNext()) {
            val pipe = iterator.next()
            pipe.x -= 12

            // Score
            if (!pipe.passed && playerX > pipe.x + pipeWidth) {
                score++
                pipe.passed = true
            }

            // Collision
            if (checkCollision(pipe)) {
                gameOver()
                return
            }

            // Remove pipe and add new one
            if (pipe.x + pipeWidth < 0) {
                iterator.remove()
                pipes.add(Pipe(screenWidth + 200))
            }
        }
    }

    private fun draw() {
        if (!holder.surface.isValid) return
        val canvas = holder.lockCanvas()

        // Background
        canvas.drawBitmap(bgBitmap, bgX.toFloat(), 0f, null)
        canvas.drawBitmap(bgBitmap, (bgX + screenWidth).toFloat(), 0f, null)

        // Pipes
        for (pipe in pipes) {
            // Top pipe
            canvas.drawBitmap(pipeBitmap, null, Rect(pipe.x, 0, pipe.x + pipeWidth, pipe.top.toInt()), null)
            // Bottom pipe
            canvas.drawBitmap(pipeBitmap, null, Rect(pipe.x, pipe.bottom.toInt(), pipe.x + pipeWidth, screenHeight), null)
        }

        // Player
        canvas.drawBitmap(playerBitmap, playerX.toFloat(), playerY.toFloat(), null)

        // Score
        canvas.drawText("Score: $score", 50f, 100f, scorePaint)

        holder.unlockCanvasAndPost(canvas)
    }

    fun onTouch() {
        velocity = -30
    }

    fun pause() {
        isPlaying = false
        thread?.join()
    }

    fun resume() {
        isPlaying = true
        thread = Thread(this)
        thread?.start()
    }

    private fun checkCollision(pipe: Pipe): Boolean {
        return playerX + playerBitmap.width > pipe.x && playerX < pipe.x + pipeWidth &&
               (playerY < pipe.top || playerY + playerBitmap.height > pipe.bottom)
    }

    private fun gameOver() {
        Toast.makeText(context, "Game Over! Score: $score", Toast.LENGTH_SHORT).show()
        playerY = screenHeight / 2
        velocity = 0
        score = 0
        pipes.clear()
        pipes.add(Pipe(screenWidth + 200))
        pipes.add(Pipe(screenWidth + 600))
        pipes.add(Pipe(screenWidth + 1000))
    }

    inner class Pipe(var x: Int) {
        val top = Random.nextInt(200, screenHeight - gap - 200).toFloat()
        val bottom = top + gap
        var passed = false
    }
}