package com.escapegame

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.escapegame.engine.GameEngine
import com.escapegame.persistence.HighScoreStore

class MainActivity : Activity() {
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val engine = GameEngine(HighScoreStore(this))
        gameView = GameView(this, engine)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }
}
