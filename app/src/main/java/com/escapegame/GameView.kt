package com.escapegame

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.escapegame.core.GameConfig
import com.escapegame.engine.GameEngine
import com.escapegame.model.GamePhase
import com.escapegame.render.TouchGamepadLayout

/**
 * Thin Android layer: owns the surface, the render thread, and the mapping
 * from hardware keys (or touches) to engine input.
 *
 * Controls are keypad-first for flip/bar phones — every action is reachable
 * from a T9 keypad (4/6 move, 2 jump, 5 confirm/run/shoot) as well as the
 * d-pad, with WASD/space as a bonus for keyboards. On touchscreen-only
 * devices a Game Boy-style on-screen gamepad appears instead; it is never
 * shown while a physical keypad/d-pad is in use.
 */
class GameView(context: Context, private val engine: GameEngine) :
    SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    // Guards all engine access: input arrives on the UI thread while the game
    // thread runs update/draw
    private val engineLock = Any()
    private var touchMode = false

    // Hybrid phones (keypad/d-pad AND a touchscreen) are treated as pure
    // keypad devices: no shell, no on-screen controls, touches ignored.
    private val hasPhysicalDpad =
        resources.configuration.navigation == Configuration.NAVIGATION_DPAD

    init {
        holder.addCallback(this)
        isFocusable = true
        // Touchscreen-only device (no physical d-pad)? Show the on-screen
        // gamepad from the start rather than waiting for the first tap.
        val hasTouchscreen =
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
        if (!hasPhysicalDpad && hasTouchscreen) {
            touchMode = true
            engine.touchControlsEnabled = true
        }
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

    // ------------------------------------------------------------- key input

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        synchronized(engineLock) {
            val handled = when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_A -> {
                    engine.onLeft(true); true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_D -> {
                    engine.onRight(true); true
                }
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_W -> {
                    if (event.repeatCount == 0) engine.onJump(); true
                }
                KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_S -> {
                    if (event.repeatCount == 0) engine.onMenuDown(); true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_5,
                KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_ENTER -> {
                    if (event.repeatCount == 0) engine.onActionDown(); true
                }
                KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_STAR -> {
                    if (event.repeatCount == 0) engine.onPauseToggle(); true
                }
                KeyEvent.KEYCODE_POUND, KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_M -> {
                    if (event.repeatCount == 0) engine.toggleMute(); true
                }
                KeyEvent.KEYCODE_7 -> {
                    if (event.repeatCount == 0) engine.onLeaderboardKey(); true
                }
                else -> false
            }
            if (handled) {
                // Physical keys win: hide the touch gamepad
                if (touchMode) {
                    touchMode = false
                    engine.touchControlsEnabled = false
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
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
                KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_S,
                KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_STAR,
                KeyEvent.KEYCODE_POUND, KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_M,
                KeyEvent.KEYCODE_7 ->
                    Unit // handled on key-down; consume the up event
                else -> return super.onKeyUp(keyCode, event)
            }
        }
        return true
    }

    // ----------------------------------------------------------- touch input

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Physical d-pad present: this is a keypad device, touch does nothing
        if (hasPhysicalDpad) return true
        synchronized(engineLock) {
            if (!touchMode) {
                touchMode = true
                engine.touchControlsEnabled = true
            }
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    handleTouchDown(event, event.actionIndex)
                    refreshHeldControls(event, -1)
                }
                MotionEvent.ACTION_MOVE -> refreshHeldControls(event, -1)
                MotionEvent.ACTION_POINTER_UP -> refreshHeldControls(event, event.actionIndex)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> releaseAllTouchControls()
            }
        }
        return true
    }

    /** Edge-triggered actions for a newly placed finger. */
    private fun handleTouchDown(event: MotionEvent, pointerIndex: Int) {
        val control = TouchGamepadLayout.hit(worldX(event, pointerIndex), worldY(event, pointerIndex))
        if (control == TouchGamepadLayout.Control.MUTE) {
            engine.toggleMute()
            return
        }
        if (control == TouchGamepadLayout.Control.SELECT && engine.phase == GamePhase.INTRO) {
            engine.onLeaderboardKey()
            return
        }
        if (engine.phase == GamePhase.DIFFICULTY_SELECT || engine.phase == GamePhase.MODE_SELECT) {
            // Menu taps need game-world coordinates (inside the LCD in
            // touch mode, the whole screen otherwise)
            val shellX = worldX(event, pointerIndex)
            val shellY = worldY(event, pointerIndex)
            if (touchMode) {
                engine.onMenuTap(
                    (shellX - TouchGamepadLayout.screenOffsetX) / TouchGamepadLayout.screenScale,
                    (shellY - TouchGamepadLayout.screenOffsetY) / TouchGamepadLayout.screenScale
                )
            } else {
                engine.onMenuTap(shellX, shellY)
            }
            return
        }
        if (engine.phase != GamePhase.PLAYING) {
            // Overlay screens: any other tap is the confirm button
            engine.onActionDown()
            return
        }
        when (control) {
            TouchGamepadLayout.Control.DPAD_UP,
            TouchGamepadLayout.Control.BUTTON_B -> engine.onJump()
            TouchGamepadLayout.Control.BUTTON_A -> engine.onActionDown()
            TouchGamepadLayout.Control.START,
            TouchGamepadLayout.Control.SELECT -> engine.onPauseToggle()
            else -> Unit
        }
    }

    /** Recomputes all hold-style controls from the fingers still down. */
    private fun refreshHeldControls(event: MotionEvent, excludeIndex: Int) {
        if (engine.phase != GamePhase.PLAYING) return
        var left = false
        var right = false
        var runHeld = false
        for (i in 0 until event.pointerCount) {
            if (i == excludeIndex) continue
            when (TouchGamepadLayout.hit(worldX(event, i), worldY(event, i))) {
                TouchGamepadLayout.Control.DPAD_LEFT -> left = true
                TouchGamepadLayout.Control.DPAD_RIGHT -> right = true
                TouchGamepadLayout.Control.BUTTON_A -> runHeld = true
                else -> Unit
            }
        }
        engine.onLeft(left)
        engine.onRight(right)
        engine.onRunHeld(runHeld)
    }

    private fun releaseAllTouchControls() {
        engine.onLeft(false)
        engine.onRight(false)
        engine.onRunHeld(false)
        engine.onActionUp()
    }

    private fun worldX(event: MotionEvent, pointerIndex: Int): Float =
        event.getX(pointerIndex) / width * GameConfig.WORLD_WIDTH

    private fun worldY(event: MotionEvent, pointerIndex: Int): Float =
        event.getY(pointerIndex) / height * GameConfig.WORLD_HEIGHT

    // ------------------------------------------------------------ game loop

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
