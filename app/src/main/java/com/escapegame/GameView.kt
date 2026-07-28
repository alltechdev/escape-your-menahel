package com.escapegame

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.escapegame.core.GameConfig
import com.escapegame.engine.GameEngine

/**
 * Thin Android layer: owns the surface, the render thread, and the mapping
 * from hardware keys to engine input.
 *
 * Controls are keypad-first for flip/bar phones — every action is reachable
 * from a T9 keypad (4/6 move, 2 jump, 5 confirm/run/shoot) as well as the
 * d-pad, with WASD/space as a bonus for keyboards.
 */
class GameView(context: Context, private val engine: GameEngine) :
    SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    // Guards all engine access: input arrives on the UI thread while the game
    // thread runs update/draw
    private val engineLock = Any()

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startGameThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // The engine simulates in a fixed virtual world; nothing to recompute.
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGameThread()
    }

    fun resume() {
        startGameThread()
    }

    fun pause() {
        stopGameThread()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        synchronized(engineLock) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_A ->
                    engine.onLeft(true)
                KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_D ->
                    engine.onRight(true)
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_W ->
                    engine.onJump()
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_5,
                KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER ->
                    engine.onActionDown()
                KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_STAR ->
                    engine.onPauseToggle()
                else -> return super.onKeyDown(keyCode, event)
            }
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        synchronized(engineLock) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_A ->
                    engine.onLeft(false)
                KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_D ->
                    engine.onRight(false)
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_5,
                KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER ->
                    engine.onActionUp()
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_W,
                KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_STAR ->
                    Unit // handled on key-down; consume the up event
                else -> return super.onKeyUp(keyCode, event)
            }
        }
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Keypad/d-pad only; touch is intentionally a no-op
        return true
    }

    // A stopped Thread cannot be restarted, so each start creates a fresh one
    private fun startGameThread() {
        if (gameThread?.isAlive == true) return
        if (holder.surface?.isValid != true) return
        gameThread = GameThread(holder).also {
            it.running = true
            it.start()
        }
    }

    private fun stopGameThread() {
        val thread = gameThread ?: return
        thread.running = false
        while (true) {
            try {
                thread.join()
                break
            } catch (e: InterruptedException) {
                // Will try again
            }
        }
        gameThread = null
    }

    private inner class GameThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        @Volatile
        var running = false

        override fun run() {
            while (running) {
                val canvas = surfaceHolder.lockCanvas()
                canvas?.let {
                    synchronized(engineLock) {
                        engine.update()
                        engine.draw(it, width, height)
                    }
                    surfaceHolder.unlockCanvasAndPost(it)
                }
                try {
                    sleep(GameConfig.FRAME_MILLIS)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }
}
