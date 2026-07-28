package com.escapegame

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.escapegame.audio.MusicEngine
import com.escapegame.engine.GameEngine
import com.escapegame.persistence.GamePrefs

class MainActivity : Activity() {
    private lateinit var gameView: GameView
    private lateinit var music: MusicEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        music = MusicEngine()
        val engine = GameEngine(GamePrefs(this), music)
        gameView = GameView(this, engine)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
        music.start()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
        music.stop()
    }
}
